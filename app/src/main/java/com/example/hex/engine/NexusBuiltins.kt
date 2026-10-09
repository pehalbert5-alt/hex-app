package com.example.hex.engine

import java.io.File
import kotlin.math.*
import kotlin.random.Random

object NexusBuiltins {

    fun getBuiltins(
        output: (String) -> Unit,
        inputPrompt: (String) -> String,
        workspaceDir: File
    ): Map<String, (List<Any?>) -> Any?> {
        val functions = mutableMapOf<String, (List<Any?>) -> Any?>()

        functions["afficher"] = { args ->
            val str = args.joinToString(" ") { formatValue(it) }
            output(str)
            null
        }

        functions["saisir"] = { args ->
            val prompt = if (args.isNotEmpty()) formatValue(args[0]) else ""
            inputPrompt(prompt)
        }

        functions["longueur"] = { args ->
            when (val obj = args.firstOrNull()) {
                is String -> obj.length.toLong()
                is List<*> -> obj.size.toLong()
                is Map<*, *> -> obj.size.toLong()
                else -> 0L
            }
        }

        functions["texte"] = { args ->
            formatValue(args.firstOrNull())
        }

        functions["entier"] = { args ->
            when (val v = args.firstOrNull()) {
                is Number -> v.toLong()
                is String -> v.trim().toLongOrNull() ?: 0L
                is Boolean -> if (v) 1L else 0L
                else -> 0L
            }
        }

        functions["decimal"] = { args ->
            when (val v = args.firstOrNull()) {
                is Number -> v.toDouble()
                is String -> v.trim().toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        }

        functions["absolu"] = { args ->
            when (val v = args.firstOrNull()) {
                is Long -> abs(v)
                is Double -> abs(v)
                is Int -> abs(v)
                else -> 0L
            }
        }

        functions["arrondir"] = { args ->
            val v = (args.firstOrNull() as? Number)?.toDouble() ?: 0.0
            round(v)
        }

        functions["minimum"] = { args ->
            if (args.isEmpty()) 0L
            else if (args.size == 1 && args[0] is List<*>) {
                val list = args[0] as List<*>
                list.filterIsInstance<Number>().minOfOrNull { it.toDouble() } ?: 0L
            } else {
                args.filterIsInstance<Number>().minOfOrNull { it.toDouble() } ?: 0L
            }
        }

        functions["maximum"] = { args ->
            if (args.isEmpty()) 0L
            else if (args.size == 1 && args[0] is List<*>) {
                val list = args[0] as List<*>
                list.filterIsInstance<Number>().maxOfOrNull { it.toDouble() } ?: 0L
            } else {
                args.filterIsInstance<Number>().maxOfOrNull { it.toDouble() } ?: 0L
            }
        }

        functions["somme"] = { args ->
            val list = (args.firstOrNull() as? List<*>) ?: args
            var total = 0.0
            for (item in list) {
                if (item is Number) total += item.toDouble()
            }
            if (total == total.toLong().toDouble()) total.toLong() else total
        }

        functions["racine"] = { args ->
            val v = (args.firstOrNull() as? Number)?.toDouble() ?: 0.0
            sqrt(v)
        }

        functions["puissance"] = { args ->
            val base = (args.getOrNull(0) as? Number)?.toDouble() ?: 0.0
            val exp = (args.getOrNull(1) as? Number)?.toDouble() ?: 1.0
            base.pow(exp)
        }

        functions["ajouter"] = { args ->
            val list = args.getOrNull(0) as? MutableList<Any?>
            val item = args.getOrNull(1)
            list?.add(item)
            list
        }

        functions["retirer"] = { args ->
            val list = args.getOrNull(0) as? MutableList<Any?>
            val idx = (args.getOrNull(1) as? Number)?.toInt() ?: -1
            if (list != null && idx in 0 until list.size) {
                list.removeAt(idx)
            }
            list
        }

        functions["trier"] = { args ->
            val list = args.getOrNull(0) as? List<Any?> ?: emptyList()
            list.sortedBy { it.toString() }
        }

        functions["inverser"] = { args ->
            val list = args.getOrNull(0) as? List<Any?> ?: emptyList()
            list.reversed()
        }

        functions["majuscules"] = { args ->
            args.firstOrNull()?.toString()?.uppercase() ?: ""
        }

        functions["minuscules"] = { args ->
            args.firstOrNull()?.toString()?.lowercase() ?: ""
        }

        functions["separer"] = { args ->
            val str = args.getOrNull(0)?.toString() ?: ""
            val sep = args.getOrNull(1)?.toString() ?: " "
            str.split(sep)
        }

        functions["joindre"] = { args ->
            val list = args.getOrNull(0) as? List<*> ?: emptyList<Any?>()
            val sep = args.getOrNull(1)?.toString() ?: ""
            list.joinToString(sep) { formatValue(it) }
        }

        functions["contient"] = { args ->
            val target = args.getOrNull(0)
            val item = args.getOrNull(1)
            when (target) {
                is String -> target.contains(item.toString())
                is List<*> -> target.contains(item)
                is Map<*, *> -> target.containsKey(item)
                else -> false
            }
        }

        functions["remplacer"] = { args ->
            val str = args.getOrNull(0)?.toString() ?: ""
            val oldS = args.getOrNull(1)?.toString() ?: ""
            val newS = args.getOrNull(2)?.toString() ?: ""
            str.replace(oldS, newS)
        }

        functions["intervalle"] = { args ->
            val start = (args.getOrNull(0) as? Number)?.toLong() ?: 0L
            val end = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
            (start..end).toList()
        }

        functions["aleatoire"] = { args ->
            val min = (args.getOrNull(0) as? Number)?.toInt() ?: 0
            val max = (args.getOrNull(1) as? Number)?.toInt() ?: 100
            if (min <= max) Random.nextInt(min, max + 1) else min
        }

        functions["type"] = { args ->
            when (args.firstOrNull()) {
                is Long, is Int -> "entier"
                is Double, is Float -> "décimal"
                is String -> "texte"
                is Boolean -> "booléen"
                is List<*> -> "liste"
                is Map<*, *> -> "objet"
                null -> "rien"
                else -> "objet"
            }
        }

        functions["lire_fichier"] = { args ->
            val relativePath = args.firstOrNull()?.toString() ?: ""
            val file = File(workspaceDir, relativePath)
            if (file.exists()) file.readText() else ""
        }

        functions["ecrire_fichier"] = { args ->
            val relativePath = args.getOrNull(0)?.toString() ?: ""
            val content = args.getOrNull(1)?.toString() ?: ""
            val file = File(workspaceDir, relativePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        }

        functions["existe"] = { args ->
            val relativePath = args.firstOrNull()?.toString() ?: ""
            File(workspaceDir, relativePath).exists()
        }

        functions["cles"] = { args ->
            val map = args.firstOrNull() as? Map<*, *> ?: emptyMap<Any?, Any?>()
            map.keys.toList()
        }

        functions["valeurs"] = { args ->
            val map = args.firstOrNull() as? Map<*, *> ?: emptyMap<Any?, Any?>()
            map.values.toList()
        }

        functions["maintenant"] = { _ ->
            System.currentTimeMillis()
        }

        functions["attendre"] = { args ->
            val ms = (args.firstOrNull() as? Number)?.toLong() ?: 0L
            try { Thread.sleep(ms) } catch (_: Exception) {}
            null
        }

        return functions
    }

    fun formatValue(v: Any?): String {
        return when (v) {
            null -> "rien"
            true -> "vrai"
            false -> "faux"
            is Double -> if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
            is List<*> -> "[${v.joinToString(", ") { formatValue(it) }}]"
            is Map<*, *> -> "{${v.entries.joinToString(", ") { "${formatValue(it.key)}: ${formatValue(it.value)}" }}}"
            else -> v.toString()
        }
    }
}
