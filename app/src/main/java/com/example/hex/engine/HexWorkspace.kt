package com.example.hex.engine

import android.content.Context
import java.io.File

class HexWorkspace(private val context: Context) {

    val rootDir: File = File(context.filesDir, "hex_workspace").apply { mkdirs() }

    init {
        initDefaultExamples()
    }

    private fun initDefaultExamples() {
        val exemplesDir = File(rootDir, "exemples").apply { mkdirs() }

        // complet.nx
        val completFile = File(exemplesDir, "complet.nx")
        if (!completFile.exists()) {
            completFile.writeText("""
                # Programme d'exemple complet NEXUS en français
                afficher "Bienvenue dans HEX Terminal !"

                variable x = 10
                constante MAX = 50

                si x < MAX alors
                    afficher "x est plus petit que MAX"
                sinon
                    afficher "x a atteint MAX"
                fin

                fonction calculer_carre(n)
                    retourner n * n
                fin

                pour i de 1 jusqu_a 5 faire
                    afficher "Le carré de " + i + " est : " + calculer_carre(i)
                fin

                variable notes = [14, 18, 12, 16]
                afficher "Nombre de notes : " + longueur(notes)
                afficher "Somme des notes : " + somme(notes)
                afficher "Moyenne : " + (somme(notes) / longueur(notes))
            """.trimIndent())
        }

        // factorielle.nx
        val factFile = File(exemplesDir, "factorielle.nx")
        if (!factFile.exists()) {
            factFile.writeText("""
                fonction factorielle(n)
                    si n <= 1 alors
                        retourner 1
                    sinon
                        retourner n * factorielle(n - 1)
                    fin
                fin

                pour i de 1 jusqu_a 8 faire
                    afficher "Factorielle de " + i + " = " + factorielle(i)
                fin
            """.trimIndent())
        }

        // moyenne.pyfr
        val pyfrFile = File(exemplesDir, "moyenne.pyfr")
        if (!pyfrFile.exists()) {
            pyfrFile.writeText("""
                # Calcul de la moyenne d'une liste de notes en Python français
                fonction moyenne(notes):
                    retourner somme(notes) / longueur(notes)

                notes = [12, 8, 15, 9]
                pour note dans trier(notes):
                    si note < 10:
                        afficher("faible :", note)

                afficher(f"Moyenne : {moyenne(notes):.1f}")
            """.trimIndent())
        }

        // Bonjour.javafr
        val javafrFile = File(exemplesDir, "Bonjour.javafr")
        if (!javafrFile.exists()) {
            javafrFile.writeText("""
                publique classe Bonjour {
                    statique entier carré(entier n) {
                        retourner n * n;
                    }

                    publique statique vide principal(Chaîne[] args) {
                        entier total = 0;
                        pour (entier i = 1; i <= 5; i++) {
                            total += carré(i);
                        }
                        Système.sortie.afficherln("Somme des carrés : " + total);
                    }
                }
            """.trimIndent())
        }

        // script.jsfr
        val jsfrFile = File(exemplesDir, "script.jsfr")
        if (!jsfrFile.exists()) {
            jsfrFile.writeText("""
                constante nombres = [1, 2, 3, 4];
                fonction doubler(valeur) {
                    retourner valeur * 2;
                }
                variable total = 0;
                pour (constante n de nombres) {
                    total += doubler(n);
                }
                afficher("Total doublé : " + total);
            """.trimIndent())
        }

        // salut.cfr
        val cfrFile = File(exemplesDir, "salut.cfr")
        if (!cfrFile.exists()) {
            cfrFile.writeText("""
                #inclure <stdio.h>

                entier principal() {
                    entier somme = 0;
                    pour (entier i = 1; i <= 4; i++) somme += i;
                    afficher("Somme = %d\n", somme);
                    retourner 0;
                }
            """.trimIndent())
        }

        // compteur.cppfr
        val cppfrFile = File(exemplesDir, "compteur.cppfr")
        if (!cppfrFile.exists()) {
            cppfrFile.writeText("""
                #inclure <iostream>
                utilisant espace_de_noms std;

                classe Compteur {
                publique:
                    entier valeur = 0;
                    vide incrémenter() { valeur++; }
                };

                entier principal() {
                    Compteur c;
                    c.incrémenter();
                    c.incrémenter();
                    std::sortie << "Valeur : " << c.valeur << std::fin_ligne;
                    retourner 0;
                }
            """.trimIndent())
        }

        // Site web français
        val siteDir = File(exemplesDir, "site_francais").apply { mkdirs() }
        val htmlfr = File(siteDir, "index.htmlfr")
        if (!htmlfr.exists()) {
            htmlfr.writeText("""
                <document langue="fr">
                <tête>
                    <meta jeu-de-caractères="UTF-8">
                    <titre>Mon site en français</titre>
                    <ressource relation="feuille-de-style" adresse="style.css">
                </tête>
                <corps>
                    <principal classe="conteneur">
                        <titre1>Bienvenue sur mon site HEX</titre1>
                        <paragraphe>Cette page est écrite en HTML, CSS et JS français !</paragraphe>
                        <bouton identifiant="btn">Cliquer ici</bouton>
                        <paragraphe identifiant="message"></paragraphe>
                    </principal>
                    <script source="script.js"></script>
                </corps>
                </document>
            """.trimIndent())
        }

        val cssfr = File(siteDir, "style.cssfr")
        if (!cssfr.exists()) {
            cssfr.writeText("""
                corps {
                    police: Arial, sans-serif;
                    couleur-de-fond: #1e1e2e;
                    couleur: blanc;
                    marge: 0;
                    remplissage: 20px;
                }
                .conteneur {
                    alignement-du-texte: centre;
                    marge: 40px auto;
                }
                titre1 {
                    couleur: orange;
                }
                bouton {
                    remplissage: 12px 24px;
                    rayon-de-bordure: 8px;
                    couleur-de-fond: #89b4fa;
                    couleur: noir;
                    bordure: aucun;
                    curseur: pointeur;
                }
            """.trimIndent())
        }

        val siteJsfr = File(siteDir, "script.jsfr")
        if (!siteJsfr.exists()) {
            siteJsfr.writeText("""
                constante btn = document.getElementById("btn");
                constante msg = document.getElementById("message");
                btn.addEventListener("click", fonction() {
                    msg.textContent = "Bonjour depuis JavaScript en français !";
                });
            """.trimIndent())
        }

        // Documentation fr
        val docDir = File(rootDir, "documentation/fr").apply { mkdirs() }
        val readme = File(docDir, "README.md")
        if (!readme.exists()) {
            readme.writeText("""
                # HEX Terminal Android — Documentation

                HEX est un terminal et un environnement de développement en français.

                ## Commandes disponibles
                - `aide` : Affiche l'aide
                - `créer projet <nom>` : Crée un projet NEXUS
                - `créer site <nom>` : Crée un projet Web
                - `exécuter <fichier>` : Lance un script (.nx, .pyfr, .jsfr, .htmlfr)
                - `traduire <fichier>` : Traduit entre le français et le vrai code
                - `site construire <dossier>` : Compile un site web français
                - `langages` : Affiche le vocabulaire des langages français
                - `ls` / `cd` / `cat` / `mkdir` / `rm` / `pwd` : Commandes système
            """.trimIndent())
        }
    }

    fun createProject(name: String, currentDir: File): String {
        val projDir = File(currentDir, name)
        if (projDir.exists()) return "Le dossier '${projDir.name}' existe déjà."
        projDir.mkdirs()

        File(projDir, "projet.hex.json").writeText("""
            {
              "nom": "$name",
              "version": "0.1.0",
              "principal": "src/principal.nx"
            }
        """.trimIndent())

        val srcDir = File(projDir, "src").apply { mkdirs() }
        File(srcDir, "principal.nx").writeText("""
            # Projet $name
            afficher "Bienvenue dans $name !"
        """.trimIndent())

        File(projDir, "construction").mkdirs()
        File(projDir, "tests").mkdirs()

        return "Projet '$name' créé avec succès dans ${projDir.path}."
    }

    fun buildSite(dir: File): String {
        if (!dir.exists() || !dir.isDirectory) return "Dossier introuvable: ${dir.path}"
        val outDir = File(dir, "construction").apply { mkdirs() }

        var count = 0
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val name = file.name
                when {
                    name.endsWith(".htmlfr") -> {
                        val realName = name.removeSuffix(".htmlfr") + ".html"
                        val transpiled = FrenchTranspiler.transpileHtml(file.readText(), true)
                        File(outDir, realName).writeText(transpiled.code)
                        count++
                    }
                    name.endsWith(".cssfr") -> {
                        val realName = name.removeSuffix(".cssfr") + ".css"
                        val transpiled = FrenchTranspiler.transpileCss(file.readText(), true)
                        File(outDir, realName).writeText(transpiled.code)
                        count++
                    }
                    name.endsWith(".jsfr") -> {
                        val realName = name.removeSuffix(".jsfr") + ".js"
                        val transpiled = FrenchTranspiler.transpileGenericWordByWord(file.readText(), FrenchTables.JS_MOTS + FrenchTables.JS_BIBLIO, true)
                        File(outDir, realName).writeText(transpiled.code)
                        count++
                    }
                }
            }
        }

        return "Site '${dir.name}' : $count fichier(s) traduit(s) dans ${outDir.path}."
    }
}
