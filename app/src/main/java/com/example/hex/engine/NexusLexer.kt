package com.example.hex.engine

enum class TokenType {
    VARIABLE, CONSTANTE, FONCTION, CLASSE, RETOURNER, SI, ALORS, SINON, FIN,
    TANT_QUE, FAIRE, POUR, CHAQUE, DANS, DE, JUSQUA, AFFICHER,
    ET, OU, NON, VRAI, FAUX, RIEN, ESSAYER, ATTRAPER, LANCER, IMPORTER,
    ARRETER, CONTINUER,
    IDENT, NOMBRE, TEXTE, NL, EOF,
    OP_PLUS, OP_MOINS, OP_FOIS, OP_DIV, OP_MOD,
    OP_EGAL_EGAL, OP_DIFF, OP_INF, OP_SUP, OP_INF_EGAL, OP_SUP_EGAL,
    EGAL, PAR_OUV, PAR_FER, CROCHET_OUV, CROCHET_FER, ACCOLADE_OUV, ACCOLADE_FER,
    VIRGULE, POINT, DEUX_POINTS
}

data class Token(
    val type: TokenType,
    val value: Any?,
    val line: Int,
    val column: Int
)

class NexusLexer(private val source: String) {

    private val keywords = mapOf(
        "variable" to TokenType.VARIABLE,
        "constante" to TokenType.CONSTANTE,
        "fonction" to TokenType.FONCTION,
        "classe" to TokenType.CLASSE,
        "retourner" to TokenType.RETOURNER,
        "si" to TokenType.SI,
        "alors" to TokenType.ALORS,
        "sinon" to TokenType.SINON,
        "fin" to TokenType.FIN,
        "tant_que" to TokenType.TANT_QUE,
        "faire" to TokenType.FAIRE,
        "pour" to TokenType.POUR,
        "chaque" to TokenType.CHAQUE,
        "dans" to TokenType.DANS,
        "de" to TokenType.DE,
        "jusqu_a" to TokenType.JUSQUA,
        "jusqua" to TokenType.JUSQUA,
        "afficher" to TokenType.AFFICHER,
        "et" to TokenType.ET,
        "ou" to TokenType.OU,
        "non" to TokenType.NON,
        "vrai" to TokenType.VRAI,
        "faux" to TokenType.FAUX,
        "rien" to TokenType.RIEN,
        "essayer" to TokenType.ESSAYER,
        "attraper" to TokenType.ATTRAPER,
        "lancer" to TokenType.LANCER,
        "importer" to TokenType.IMPORTER,
        "arreter" to TokenType.ARRETER,
        "continuer" to TokenType.CONTINUER
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = source.length
        var line = 1
        var lineStart = 0

        while (i < len) {
            val c = source[i]
            val col = i - lineStart + 1

            when {
                c == '\n' -> {
                    tokens.add(Token(TokenType.NL, "\n", line, col))
                    line++
                    i++
                    lineStart = i
                }
                c.isWhitespace() -> {
                    i++
                }
                c == '#' -> {
                    while (i < len && source[i] != '\n') {
                        i++
                    }
                }
                c == '"' -> {
                    val startCol = col
                    i++
                    val sb = StringBuilder()
                    while (i < len && source[i] != '"' && source[i] != '\n') {
                        if (source[i] == '\\' && i + 1 < len) {
                            i++
                            when (source[i]) {
                                'n' -> sb.append('\n')
                                't' -> sb.append('\t')
                                else -> sb.append(source[i])
                            }
                        } else {
                            sb.append(source[i])
                        }
                        i++
                    }
                    if (i < len && source[i] == '"') {
                        i++
                    }
                    tokens.add(Token(TokenType.TEXTE, sb.toString(), line, startCol))
                }
                c.isDigit() -> {
                    val startI = i
                    while (i < len && source[i].isDigit()) {
                        i++
                    }
                    if (i + 1 < len && source[i] == '.' && source[i + 1].isDigit()) {
                        i++
                        while (i < len && source[i].isDigit()) {
                            i++
                        }
                        val num = source.substring(startI, i).toDouble()
                        tokens.add(Token(TokenType.NOMBRE, num, line, col))
                    } else {
                        val num = source.substring(startI, i).toLong()
                        tokens.add(Token(TokenType.NOMBRE, num, line, col))
                    }
                }
                c.isLetter() || c == '_' -> {
                    val startI = i
                    while (i < len && (source[i].isLetterOrDigit() || source[i] == '_')) {
                        i++
                    }
                    val word = source.substring(startI, i)
                    val normWord = FrenchTables.removeAccents(word.lowercase())
                    val type = keywords[normWord] ?: TokenType.IDENT
                    tokens.add(Token(type, word, line, col))
                }
                else -> {
                    when (c) {
                        '=' -> {
                            if (i + 1 < len && source[i + 1] == '=') {
                                tokens.add(Token(TokenType.OP_EGAL_EGAL, "==", line, col))
                                i += 2
                            } else {
                                tokens.add(Token(TokenType.EGAL, "=", line, col))
                                i++
                            }
                        }
                        '!' -> {
                            if (i + 1 < len && source[i + 1] == '=') {
                                tokens.add(Token(TokenType.OP_DIFF, "!=", line, col))
                                i += 2
                            } else {
                                i++
                            }
                        }
                        '<' -> {
                            if (i + 1 < len && source[i + 1] == '=') {
                                tokens.add(Token(TokenType.OP_INF_EGAL, "<=", line, col))
                                i += 2
                            } else {
                                tokens.add(Token(TokenType.OP_INF, "<", line, col))
                                i++
                            }
                        }
                        '>' -> {
                            if (i + 1 < len && source[i + 1] == '=') {
                                tokens.add(Token(TokenType.OP_SUP_EGAL, ">=", line, col))
                                i += 2
                            } else {
                                tokens.add(Token(TokenType.OP_SUP, ">", line, col))
                                i++
                            }
                        }
                        '+' -> { tokens.add(Token(TokenType.OP_PLUS, "+", line, col)); i++ }
                        '-' -> { tokens.add(Token(TokenType.OP_MOINS, "-", line, col)); i++ }
                        '*' -> { tokens.add(Token(TokenType.OP_FOIS, "*", line, col)); i++ }
                        '/' -> { tokens.add(Token(TokenType.OP_DIV, "/", line, col)); i++ }
                        '%' -> { tokens.add(Token(TokenType.OP_MOD, "%", line, col)); i++ }
                        '(' -> { tokens.add(Token(TokenType.PAR_OUV, "(", line, col)); i++ }
                        ')' -> { tokens.add(Token(TokenType.PAR_FER, ")", line, col)); i++ }
                        '[' -> { tokens.add(Token(TokenType.CROCHET_OUV, "[", line, col)); i++ }
                        ']' -> { tokens.add(Token(TokenType.CROCHET_FER, "]", line, col)); i++ }
                        '{' -> { tokens.add(Token(TokenType.ACCOLADE_OUV, "{", line, col)); i++ }
                        '}' -> { tokens.add(Token(TokenType.ACCOLADE_FER, "}", line, col)); i++ }
                        ',' -> { tokens.add(Token(TokenType.VIRGULE, ",", line, col)); i++ }
                        '.' -> { tokens.add(Token(TokenType.POINT, ".", line, col)); i++ }
                        ':' -> { tokens.add(Token(TokenType.DEUX_POINTS, ":", line, col)); i++ }
                        else -> i++
                    }
                }
            }
        }

        tokens.add(Token(TokenType.EOF, null, line, 1))
        return tokens
    }
}
