package com.example.hex.engine

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.io.File

enum class LineType {
    PROMPT, OUTPUT, SUCCESS, ERROR, INFO
}

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT
)

class HexSession(
    private val context: Context,
    private val onOpenFileInEditor: (File) -> Unit,
    private val onOpenWebPreview: (File) -> Unit,
    private val onRequestInput: (prompt: String, callback: (String) -> Unit) -> Unit
) {

    val workspace = HexWorkspace(context)
    val currentDir = mutableStateOf(workspace.rootDir)
    val outputLines = mutableStateListOf<TerminalLine>()
    val history = mutableListOf<String>()

    private val jsEvaluator = JsEvaluator(context) { msg ->
        appendLine(msg, LineType.OUTPUT)
    }

    init {
        appendLine("HEX Terminal — environnement de développement français.", LineType.INFO)
        appendLine("Tapez « aide » pour voir les commandes disponibles.", LineType.INFO)
        appendLine("Repertoire courant : ${currentDir.value.name}", LineType.INFO)
        appendLine("", LineType.OUTPUT)
    }

    fun getPromptString(): String {
        val relPath = currentDir.value.path.removePrefix(workspace.rootDir.path)
        val displayPath = if (relPath.isEmpty()) "~" else "~$relPath"
        return "$displayPath HEX> "
    }

    fun appendLine(text: String, type: LineType = LineType.OUTPUT) {
        outputLines.add(TerminalLine(text, type))
    }

    fun clearScreen() {
        outputLines.clear()
    }

    fun execute(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return

        history.add(trimmed)
        appendLine("${getPromptString()}$trimmed", LineType.PROMPT)

        var parts = trimmed.split("\\s+".toRegex())
        if (parts.firstOrNull()?.lowercase() == "hex") {
            parts = parts.drop(1)
        }
        if (parts.isEmpty()) return

        val verb = FrenchTables.removeAccents(parts[0].lowercase())
        val args = parts.drop(1)

        when (verb) {
            "aide", "help" -> cmdHelp(args)
            "creer", "create" -> cmdCreate(args)
            "executer", "run" -> cmdExecute(args)
            "construire", "build" -> cmdBuild(args)
            "traduire", "translate" -> cmdTranspile(args)
            "langages", "languages" -> cmdLanguages(args)
            "site" -> cmdSite(args)
            "ls", "lister", "dir" -> cmdList(args)
            "cd" -> cmdCd(args)
            "pwd" -> appendLine(currentDir.value.path, LineType.INFO)
            "cat", "lire" -> cmdCat(args)
            "mkdir" -> cmdMkdir(args)
            "rm", "supprimer" -> cmdRm(args)
            "ouvrir", "open" -> cmdOpen(args)
            "clear", "cls", "effacer", "nettoyer" -> clearScreen()
            else -> {
                val file = File(currentDir.value, parts[0])
                if (file.exists() && file.isFile) {
                    executeFile(file)
                } else {
                    appendLine("Commande inconnue : « ${parts[0]} ». Tapez « aide ».", LineType.ERROR)
                }
            }
        }
    }

    private fun cmdHelp(args: List<String>) {
        appendLine("Commandes HEX Terminal :", LineType.SUCCESS)
        appendLine("  créer projet <nom>     Crée un projet NEXUS", LineType.OUTPUT)
        appendLine("  créer site <nom>       Crée un site Web en français", LineType.OUTPUT)
        appendLine("  exécuter <fichier>     Exécute un fichier (.nx, .pyfr, .jsfr, .htmlfr)", LineType.OUTPUT)
        appendLine("  construire             Construit le projet courant", LineType.OUTPUT)
        appendLine("  traduire <fichier>     Traduit entre français et vrai code", LineType.OUTPUT)
        appendLine("  site construire <doss> Construit le site web du dossier", LineType.OUTPUT)
        appendLine("  langages [nom]         Affiche le vocabulaire d'un langage", LineType.OUTPUT)
        appendLine("  ls / cd / cat / mkdir  Navigation et gestion des fichiers", LineType.OUTPUT)
        appendLine("  ouvrir <fichier>       Ouvre un fichier dans l'éditeur", LineType.OUTPUT)
        appendLine("  effacer / clear        Efface l'écran du terminal", LineType.OUTPUT)
    }

    private fun cmdCreate(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: créer projet <nom> | créer site <nom>", LineType.ERROR)
            return
        }
        val type = FrenchTables.removeAccents(args[0].lowercase())
        val name = args.getOrNull(1) ?: "mon_projet"

        when (type) {
            "projet", "project" -> {
                val msg = workspace.createProject(name, currentDir.value)
                appendLine(msg, LineType.SUCCESS)
            }
            "site", "web" -> {
                val siteDir = File(currentDir.value, name).apply { mkdirs() }
                File(siteDir, "index.htmlfr").writeText("""
                    <document langue="fr">
                    <tête>
                        <titre>$name</titre>
                        <ressource relation="feuille-de-style" adresse="style.css">
                    </tête>
                    <corps>
                        <titre1>Bienvenue sur $name !</titre1>
                        <script source="script.js"></script>
                    </corps>
                    </document>
                """.trimIndent())
                File(siteDir, "style.cssfr").writeText("corps { couleur: blanc; couleur-de-fond: noir; }")
                File(siteDir, "script.jsfr").writeText("afficher(\"Site $name prêt !\");")
                appendLine("Site '$name' créé avec succès.", LineType.SUCCESS)
            }
            else -> {
                val msg = workspace.createProject(type, currentDir.value)
                appendLine(msg, LineType.SUCCESS)
            }
        }
    }

    private fun cmdExecute(args: List<String>) {
        if (args.isEmpty()) {
            val mainFile = File(currentDir.value, "src/principal.nx")
            if (mainFile.exists()) {
                executeFile(mainFile)
            } else {
                appendLine("Usage: exécuter <fichier>", LineType.ERROR)
            }
            return
        }
        val target = File(currentDir.value, args[0])
        if (!target.exists()) {
            appendLine("Fichier introuvable : ${args[0]}", LineType.ERROR)
            return
        }
        executeFile(target)
    }

    fun executeFile(file: File) {
        val ext = file.extension.lowercase()
        appendLine("► Exécution de ${file.name}...", LineType.INFO)

        when (ext) {
            "nx" -> {
                try {
                    val interpreter = NexusInterpreter(
                        output = { msg -> appendLine(msg, LineType.OUTPUT) },
                        inputPrompt = { prompt ->
                            var result = ""
                            onRequestInput(prompt) { inputVal ->
                                result = inputVal
                            }
                            result
                        },
                        workspaceDir = currentDir.value
                    )
                    val tokens = NexusLexer(file.readText()).tokenize()
                    val program = NexusParser(tokens).parseProgram()
                    interpreter.executeProgram(program)
                    appendLine("✔ Exécution terminée.", LineType.SUCCESS)
                } catch (e: Exception) {
                    appendLine("✖ Erreur d'exécution NEXUS : ${e.message}", LineType.ERROR)
                }
            }
            "pyfr", "py" -> {
                val transpiled = FrenchTranspiler.transpilePython(file.readText(), toReal = true)
                appendLine("--- Code Python réel généré ---", LineType.INFO)
                appendLine(transpiled.code, LineType.OUTPUT)
                appendLine("--- Simulation d'exécution Python ---", LineType.INFO)
                // Execute python via Nexus/JS simulator
                val simulatedJs = transpiled.code
                    .replace("print(", "console.log(")
                    .replace("def ", "function ")
                    .replace("True", "true")
                    .replace("False", "false")
                jsEvaluator.evaluate(simulatedJs)
            }
            "jsfr", "js" -> {
                val transpiled = FrenchTranspiler.transpileGenericWordByWord(file.readText(), FrenchTables.JS_MOTS + FrenchTables.JS_BIBLIO, toReal = true)
                appendLine("--- Code JavaScript réel ---", LineType.INFO)
                appendLine(transpiled.code, LineType.OUTPUT)
                appendLine("--- Exécution JavaScript V8 ---", LineType.INFO)
                jsEvaluator.evaluate(transpiled.code)
            }
            "htmlfr", "html" -> {
                val transpiled = FrenchTranspiler.transpileHtml(file.readText(), toReal = true)
                val outDir = File(currentDir.value, "construction").apply { mkdirs() }
                val htmlFile = File(outDir, "index.html")
                htmlFile.writeText(transpiled.code)
                appendLine("✔ Page web générée dans ${htmlFile.path}", LineType.SUCCESS)
                onOpenWebPreview(htmlFile)
            }
            "javafr", "cfr", "cppfr" -> {
                val transpiled = FrenchTranspiler.transpileFile(file.readText(), file.name, toReal = true)
                appendLine("--- Code réel généré (${file.extension}) ---", LineType.INFO)
                appendLine(transpiled.code, LineType.OUTPUT)
                appendLine("✔ Transpilation terminée avec succès.", LineType.SUCCESS)
            }
            else -> {
                appendLine("Format non directement exécutable : .$ext", LineType.ERROR)
            }
        }
    }

    private fun cmdBuild(args: List<String>) {
        val srcDir = File(currentDir.value, "src")
        val constrDir = File(currentDir.value, "construction").apply { mkdirs() }

        if (!srcDir.exists()) {
            appendLine("Dossier 'src/' introuvable.", LineType.ERROR)
            return
        }

        var count = 0
        srcDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val transpiled = FrenchTranspiler.transpileFile(file.readText(), file.name, toReal = true)
                val outFile = File(constrDir, file.name.removeSuffix("fr"))
                outFile.writeText(transpiled.code)
                appendLine("  ✔ ${file.name} -> ${outFile.name}", LineType.SUCCESS)
                count++
            }
        }
        appendLine("Construction terminée : $count fichier(s).", LineType.SUCCESS)
    }

    private fun cmdTranspile(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: traduire <fichier> [--vers fr]", LineType.ERROR)
            return
        }
        val fileName = args[0]
        val file = File(currentDir.value, fileName)
        if (!file.exists()) {
            appendLine("Fichier introuvable : $fileName", LineType.ERROR)
            return
        }

        val toFr = args.contains("--vers") && args.contains("fr")
        val res = FrenchTranspiler.transpileFile(file.readText(), file.name, toReal = !toFr)

        appendLine("--- Résultat de la traduction ---", LineType.INFO)
        appendLine(res.code, LineType.OUTPUT)
    }

    private fun cmdLanguages(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Langages en français disponibles :", LineType.SUCCESS)
            appendLine("  python (.pyfr), java (.javafr), javascript (.jsfr), c (.cfr), cpp (.cppfr), html (.htmlfr), css (.cssfr)", LineType.OUTPUT)
            appendLine("Utilisez 'langages python' pour voir le vocabulaire.", LineType.INFO)
            return
        }
        val lang = FrenchTables.removeAccents(args[0].lowercase())
        when (lang) {
            "python" -> {
                appendLine("Mots-clés Python français :", LineType.SUCCESS)
                FrenchTables.PY_MOTS.forEach { (fr, real) -> appendLine("  $fr -> $real") }
                appendLine("Fonctions Python français :", LineType.SUCCESS)
                FrenchTables.PY_FONCTIONS.forEach { (fr, real) -> appendLine("  $fr -> $real") }
            }
            "js", "javascript" -> {
                appendLine("Mots-clés JavaScript français :", LineType.SUCCESS)
                FrenchTables.JS_MOTS.forEach { (fr, real) -> appendLine("  $fr -> $real") }
            }
            "html" -> {
                appendLine("Balises HTML français :", LineType.SUCCESS)
                FrenchTables.HTML_BALISES.forEach { (fr, real) -> appendLine("  $fr -> $real") }
            }
            "css" -> {
                appendLine("Propriétés CSS français :", LineType.SUCCESS)
                FrenchTables.CSS_PROPRIETES.forEach { (fr, real) -> appendLine("  $fr -> $real") }
            }
            else -> {
                appendLine("Mots-clés $lang :", LineType.SUCCESS)
                FrenchTables.JAVA_MOTS.forEach { (fr, real) -> appendLine("  $fr -> $real") }
            }
        }
    }

    private fun cmdSite(args: List<String>) {
        if (args.size >= 2 && FrenchTables.removeAccents(args[0].lowercase()) == "construire") {
            val folder = File(currentDir.value, args[1])
            val msg = workspace.buildSite(folder)
            appendLine(msg, LineType.SUCCESS)
            val indexHtml = File(folder, "construction/index.html")
            if (indexHtml.exists()) {
                onOpenWebPreview(indexHtml)
            }
        } else {
            appendLine("Usage: site construire <dossier>", LineType.ERROR)
        }
    }

    private fun cmdList(args: List<String>) {
        val target = if (args.isNotEmpty()) File(currentDir.value, args[0]) else currentDir.value
        if (!target.exists()) {
            appendLine("Dossier introuvable.", LineType.ERROR)
            return
        }
        val files = target.listFiles()?.sortedBy { it.name } ?: emptyList()
        appendLine("Contenu de ${target.name} :", LineType.INFO)
        for (f in files) {
            val symbol = if (f.isDirectory) "📁 " else "📄 "
            appendLine("  $symbol${f.name}")
        }
    }

    private fun cmdCd(args: List<String>) {
        if (args.isEmpty() || args[0] == "~") {
            currentDir.value = workspace.rootDir
            appendLine("Repertoire : ~", LineType.INFO)
            return
        }
        if (args[0] == "..") {
            if (currentDir.value.path != workspace.rootDir.path) {
                currentDir.value = currentDir.value.parentFile ?: workspace.rootDir
            }
            appendLine("Repertoire : ${currentDir.value.name}", LineType.INFO)
            return
        }
        val target = File(currentDir.value, args[0])
        if (target.exists() && target.isDirectory) {
            currentDir.value = target
            appendLine("Repertoire : ${target.name}", LineType.INFO)
        } else {
            appendLine("Dossier introuvable : ${args[0]}", LineType.ERROR)
        }
    }

    private fun cmdCat(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: cat <fichier>", LineType.ERROR)
            return
        }
        val file = File(currentDir.value, args[0])
        if (file.exists() && file.isFile) {
            appendLine(file.readText(), LineType.OUTPUT)
        } else {
            appendLine("Fichier introuvable : ${args[0]}", LineType.ERROR)
        }
    }

    private fun cmdMkdir(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: mkdir <dossier>", LineType.ERROR)
            return
        }
        val dir = File(currentDir.value, args[0])
        if (dir.mkdirs()) {
            appendLine("Dossier créé : ${args[0]}", LineType.SUCCESS)
        } else {
            appendLine("Impossible de créer le dossier.", LineType.ERROR)
        }
    }

    private fun cmdRm(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: rm <fichier>", LineType.ERROR)
            return
        }
        val file = File(currentDir.value, args[0])
        if (file.exists()) {
            file.deleteRecursively()
            appendLine("Supprimé : ${args[0]}", LineType.SUCCESS)
        } else {
            appendLine("Fichier introuvable : ${args[0]}", LineType.ERROR)
        }
    }

    private fun cmdOpen(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: ouvrir <fichier>", LineType.ERROR)
            return
        }
        val file = File(currentDir.value, args[0])
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.writeText("")
        }
        onOpenFileInEditor(file)
        appendLine("Ouverture de ${file.name} dans l'éditeur...", LineType.SUCCESS)
    }
}
