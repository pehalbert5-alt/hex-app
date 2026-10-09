package com.example.hex.engine

object FrenchTranspiler {

    data class TranspilationResult(
        val code: String,
        val warnings: List<String> = emptyList()
    )

    fun transpileFile(content: String, fileName: String, toReal: Boolean = true): TranspilationResult {
        val ext = fileName.substringAfterLast('.').lowercase()
        return when (ext) {
            "pyfr", "py" -> transpilePython(content, toReal)
            "javafr", "java" -> transpileGenericWordByWord(content, FrenchTables.JAVA_MOTS + FrenchTables.JAVA_BIBLIO, toReal)
            "jsfr", "js" -> transpileGenericWordByWord(content, FrenchTables.JS_MOTS + FrenchTables.JS_BIBLIO, toReal)
            "cfr", "c" -> transpileGenericWordByWord(content, FrenchTables.C_MOTS + FrenchTables.C_DIRECTIVES + FrenchTables.C_BIBLIO, toReal)
            "cppfr", "cpp" -> transpileGenericWordByWord(content, FrenchTables.CPP_MOTS + FrenchTables.C_DIRECTIVES + FrenchTables.CPP_BIBLIO, toReal)
            "htmlfr", "html" -> transpileHtml(content, toReal)
            "cssfr", "css" -> transpileCss(content, toReal)
            else -> TranspilationResult(content)
        }
    }

    fun transpilePython(source: String, toReal: Boolean = true): TranspilationResult {
        val map = if (toReal) {
            val combined = mutableMapOf<String, String>()
            (FrenchTables.PY_MOTS + FrenchTables.PY_FONCTIONS + FrenchTables.PY_EXCEPTIONS + FrenchTables.PY_METHODES + FrenchTables.PY_ARGUMENTS).forEach { (k, v) ->
                combined[FrenchTables.removeAccents(k)] = v
                combined[k] = v
            }
            combined["vrai"] = "True"
            combined["faux"] = "False"
            combined["rien"] = "None"
            combined
        } else {
            val combined = mutableMapOf<String, String>()
            (FrenchTables.PY_MOTS + FrenchTables.PY_FONCTIONS + FrenchTables.PY_EXCEPTIONS + FrenchTables.PY_METHODES + FrenchTables.PY_ARGUMENTS).forEach { (k, v) ->
                combined[v] = k
            }
            combined["True"] = "vrai"
            combined["False"] = "faux"
            combined["None"] = "rien"
            combined
        }

        return replaceTokensPreservingStringsAndComments(source, map, toReal)
    }

    fun transpileGenericWordByWord(source: String, table: Map<String, String>, toReal: Boolean = true): TranspilationResult {
        val map = if (toReal) {
            val combined = mutableMapOf<String, String>()
            table.forEach { (k, v) ->
                combined[FrenchTables.removeAccents(k)] = v
                combined[k] = v
            }
            combined
        } else {
            table.entries.associate { (k, v) -> v to k }
        }

        return replaceTokensPreservingStringsAndComments(source, map, toReal)
    }

    fun transpileCss(source: String, toReal: Boolean = true): TranspilationResult {
        val combinedTable = FrenchTables.CSS_PROPRIETES + FrenchTables.CSS_VALEURS + FrenchTables.CSS_FONCTIONS + FrenchTables.CSS_PSEUDO + FrenchTables.CSS_REGLES
        val map = if (toReal) {
            val combined = mutableMapOf<String, String>()
            combinedTable.forEach { (k, v) ->
                combined[FrenchTables.removeAccents(k)] = v
                combined[k] = v
            }
            combined
        } else {
            combinedTable.entries.associate { (k, v) -> v to k }
        }

        return replaceTokensPreservingStringsAndComments(source, map, toReal)
    }

    fun transpileHtml(source: String, toReal: Boolean = true): TranspilationResult {
        val warnings = mutableListOf<String>()
        var result = source

        val tagMap = if (toReal) {
            val combined = mutableMapOf<String, String>()
            FrenchTables.HTML_BALISES.forEach { (k, v) ->
                combined[FrenchTables.removeAccents(k)] = v
                combined[k] = v
            }
            combined
        } else {
            FrenchTables.HTML_BALISES.entries.associate { (k, v) -> v to k }
        }

        val attrMap = if (toReal) {
            val combined = mutableMapOf<String, String>()
            FrenchTables.HTML_ATTRIBUTS.forEach { (k, v) ->
                combined[FrenchTables.removeAccents(k)] = v
                combined[k] = v
            }
            combined
        } else {
            FrenchTables.HTML_ATTRIBUTS.entries.associate { (k, v) -> v to k }
        }

        tagMap.forEach { (frTag, realTag) ->
            result = result.replace("<$frTag", "<$realTag", ignoreCase = true)
                .replace("</$frTag>", "</$realTag>", ignoreCase = true)
        }

        attrMap.forEach { (frAttr, realAttr) ->
            result = result.replace(" $frAttr=", " $realAttr=", ignoreCase = true)
        }

        if (toReal && result.contains("<html", ignoreCase = true) && !result.contains("<!DOCTYPE", ignoreCase = true)) {
            result = "<!DOCTYPE html>\n$result"
        }

        return TranspilationResult(result, warnings)
    }

    private fun replaceTokensPreservingStringsAndComments(
        source: String,
        map: Map<String, String>,
        toReal: Boolean
    ): TranspilationResult {
        val sb = StringBuilder()
        var i = 0
        val len = source.length

        while (i < len) {
            val ch = source[i]

            if (ch == '#' || (ch == '/' && i + 1 < len && source[i + 1] == '/')) {
                val start = i
                while (i < len && source[i] != '\n') {
                    i++
                }
                sb.append(source.substring(start, i))
                continue
            }

            if (ch == '/' && i + 1 < len && source[i + 1] == '*') {
                val start = i
                i += 2
                while (i + 1 < len && !(source[i] == '*' && source[i + 1] == '/')) {
                    i++
                }
                if (i + 1 < len) i += 2
                sb.append(source.substring(start, i))
                continue
            }

            if (ch == '"' || ch == '\'') {
                val quote = ch
                val start = i
                i++
                while (i < len && source[i] != quote) {
                    if (source[i] == '\\' && i + 1 < len) {
                        i += 2
                    } else {
                        i++
                    }
                }
                if (i < len) i++
                sb.append(source.substring(start, i))
                continue
            }

            if (ch.isLetter() || ch == '_' || ch == '$' || ch == '@') {
                val start = i
                while (i < len && (source[i].isLetterOrDigit() || source[i] == '_' || source[i] == '-' || source[i] == '$' || source[i] == '@')) {
                    i++
                }
                val word = source.substring(start, i)
                val wordNorm = FrenchTables.removeAccents(word)
                val replacement = map[word] ?: map[wordNorm] ?: map[word.lowercase()] ?: map[FrenchTables.removeAccents(word.lowercase())]

                if (replacement != null) {
                    if (word.first().isUpperCase()) {
                        sb.append(replacement.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                    } else {
                        sb.append(replacement)
                    }
                } else {
                    sb.append(word)
                }
                continue
            }

            sb.append(ch)
            i++
        }

        return TranspilationResult(sb.toString())
    }
}
