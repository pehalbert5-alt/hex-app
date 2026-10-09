package com.example.hex.engine

import java.text.Normalizer

object FrenchTables {

    fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    private fun parsePairs(raw: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (token in raw.trim().split("\\s+".toRegex())) {
            if (token.contains("=")) {
                val parts = token.split("=", limit = 2)
                if (parts.size == 2) {
                    map[parts[0]] = parts[1]
                }
            }
        }
        return map
    }

    // --- PYTHON ---
    val PY_MOTS = parsePairs("""
        si=if sinon=else sinon_si=elif pour=for dans=in tant_que=while fonction=def classe=class
        retourner=return importer=import depuis=from comme=as essayer=try sauf=except finalement=finally
        lever=raise avec=with passer=pass arrêter=break continuer=continue et=and ou=or non=not est=is
        Vrai=True Faux=False Rien=None global=global non_local=nonlocal supprimer=del affirmer=assert
        produire=yield anonyme=lambda asynchrone=async attendre=await
    """.trimIndent())

    val PY_FONCTIONS = parsePairs("""
        afficher=print saisir=input longueur=len intervalle=range entier=int décimal=float texte=str
        booléen=bool liste=list dictionnaire=dict ensemble=set absolu=abs arrondir=round minimum=min
        maximum=max somme=sum trier=sorted inverser=reversed énumérer=enumerate assembler=zip
        appliquer=map filtrer=filter ouvrir=open tous=all quelconque=any instance_de=isinstance
        a_attribut=hasattr puissance=pow caractère=chr code_caractère=ord hexadécimal=hex binaire=bin
        aide=help objet=object octets=bytes propriété=property méthode_statique=staticmethod
        méthode_classe=classmethod type_de=type
    """.trimIndent())

    val PY_EXCEPTIONS = parsePairs("""
        Erreur=Exception ExceptionDeBase=BaseException ErreurDeNom=NameError ErreurDeType=TypeError
        ErreurDeValeur=ValueError ErreurDIndex=IndexError ErreurDeClé=KeyError ErreurDAttribut=AttributeError
        ErreurDImportation=ImportError ModuleIntrouvable=ModuleNotFoundError FichierIntrouvable=FileNotFoundError
        ErreurSystème=OSError DivisionParZéro=ZeroDivisionError ErreurDeSyntaxe=SyntaxError
        ErreurDExécution=RuntimeError ErreurDAssertion=AssertionError ErreurDeRécursion=RecursionError
        ErreurDeDépassement=OverflowError FinDeFichier=EOFError InterruptionClavier=KeyboardInterrupt
        FinDItération=StopIteration ErreurDIndentation=IndentationError ErreurDePermission=PermissionError
    """.trimIndent())

    val PY_METHODES = parsePairs("""
        majuscules=upper minuscules=lower capitaliser=capitalize titre=title séparer=split joindre=join
        remplacer=replace nettoyer=strip commence_par=startswith termine_par=endswith trouver=find
        compter=count formater=format est_numérique=isdigit est_alphabétique=isalpha centrer=center
        ajouter=append étendre=extend insérer=insert retirer=remove dépiler=pop trier=sort inverser=reverse
        indice=index vider=clear copier=copy clés=keys valeurs=values éléments=items obtenir=get
        mettre_à_jour=update ajouter_élément=add lire=read écrire=write fermer=close lire_ligne=readline
        lire_lignes=readlines
    """.trimIndent())

    val PY_ARGUMENTS = parsePairs("fin=end séparateur=sep inverse=reverse clé=key défaut=default fichier=file encodage=encoding")

    // --- JAVA ---
    val JAVA_MOTS = parsePairs("""
        publique=public privé=private protégé=protected statique=static abstrait=abstract classe=class
        énumération=enum étend=extends implémente=implements paquet=package importer=import nouveau=new
        retourner=return si=if sinon=else pour=for tant_que=while faire=do selon=switch cas=case
        défaut=default arrêter=break continuer=continue essayer=try attraper=catch finalement=finally
        lancer=throw lance=throws ceci=this vide=void entier=int court=short octet=byte décimal=double
        flottant=float booléen=boolean caractère=char vrai=true faux=false nul=null instance_de=instanceof
        synchronisé=synchronized principal=main
    """.trimIndent())

    val JAVA_BIBLIO = parsePairs("""
        Système=System sortie=out erreur=err entrée=in afficherln=println afficher=print afficherf=printf
        Chaîne=String longueur=length égale=equals commence_par=startsWith termine_par=endsWith
        sous_chaîne=substring majuscules=toUpperCase minuscules=toLowerCase contient=contains
        est_vide=isEmpty taille=size ajouter=add obtenir=get retirer=remove vers_chaîne=toString
        caractère_à=charAt indice_de=indexOf remplacer=replace diviser=split couper=trim Entier=Integer
        Décimal=Double Booléen=Boolean Caractère=Character Objet=Object Mathématiques=Math racine=sqrt
        puissance=pow absolu=abs maximum=max minimum=min aléatoire=random Liste=List ListeTableau=ArrayList
        Carte=Map CarteDeHachage=HashMap Ensemble=Set EnsembleDeHachage=HashSet Tableaux=Arrays
        Lecteur=Scanner prochaine_ligne=nextLine prochain_entier=nextInt prochain_décimal=nextDouble
        analyser_entier=parseInt analyser_décimal=parseDouble ErreurDExécution=RuntimeException
        quitter=exit temps_actuel=currentTimeMillis valeur_de=valueOf trier=sort
    """.trimIndent())

    // --- JAVASCRIPT ---
    val JS_MOTS = parsePairs("""
        variable=let constante=const fonction=function retourner=return si=if sinon=else pour=for
        tant_que=while faire=do selon=switch cas=case défaut=default arrêter=break continuer=continue
        essayer=try attraper=catch finalement=finally lancer=throw nouveau=new classe=class étend=extends
        ceci=this importer=import exporter=export depuis=from vrai=true faux=false nul=null indéfini=undefined
        de=of dans=in asynchrone=async attendre=await type_de=typeof instance_de=instanceof
        supprimer=delete statique=static constructeur=constructor
    """.trimIndent())

    val JS_BIBLIO = parsePairs("""
        afficher=console.log afficher_erreur=console.error alerte=alert Mathématiques=Math racine=sqrt
        puissance=pow arrondir=round plancher=floor plafond=ceil aléatoire=random absolu=abs maximum=max
        minimum=min Tableau=Array Objet=Object Chaîne=String Nombre=Number Promesse=Promise longueur=length
        ajouter=push retirer=pop joindre=join diviser=split filtrer=filter appliquer=map réduire=reduce
        trier=sort inverser=reverse inclut=includes indice_de=indexOf trancher=slice remplacer=replace
        majuscules=toUpperCase minuscules=toLowerCase pour_chaque=forEach trouver=find clés=keys valeurs=values
        fenêtre=window obtenir_élément_par_id=getElementById sélectionner=querySelector
        sélectionner_tout=querySelectorAll ajouter_écouteur=addEventListener contenu_texte=textContent
        contenu_html=innerHTML valeur=value créer_élément=createElement ajouter_enfant=appendChild
        liste_de_classes=classList après=setTimeout à_intervalle=setInterval analyser_entier=parseInt
        analyser_décimal=parseFloat analyser=parse convertir=stringify
    """.trimIndent())

    // --- C & C++ ---
    val C_MOTS = parsePairs("""
        si=if sinon=else pour=for tant_que=while faire=do selon=switch cas=case défaut=default
        arrêter=break continuer=continue retourner=return aller_à=goto structure=struct
        énumération=enum définir_type=typedef statique=static externe=extern constant=const
        vide=void entier=int court=short flottant=float décimal=double caractère=char
        non_signé=unsigned signé=signed taille_de=sizeof principal=main vrai=true faux=false
        booléen=bool NUL=NULL
    """.trimIndent())

    val C_DIRECTIVES = parsePairs("""
        inclure=include définir=define si_défini=ifdef si_non_défini=ifndef fin_si=endif annuler_définition=undef
    """.trimIndent())

    val C_BIBLIO = parsePairs("""
        afficher=printf saisir=scanf longueur_chaîne=strlen copier_chaîne=strcpy comparer_chaîne=strcmp
        concaténer_chaîne=strcat racine_carrée=sqrt puissance=pow valeur_absolue=abs aléatoire=rand
        graine_aléatoire=srand quitter=exit allouer=malloc réallouer=realloc libérer=free
        ouvrir_fichier=fopen fermer_fichier=fclose lire_ligne=fgets écrire_fichier=fprintf temps=time
    """.trimIndent())

    val CPP_MOTS = parsePairs("""
        si=if sinon=else pour=for tant_que=while faire=do selon=switch cas=case défaut=default
        arrêter=break continuer=continue retourner=return aller_à=goto structure=struct
        énumération=enum définir_type=typedef statique=static externe=extern constant=const
        vide=void entier=int court=short flottant=float décimal=double caractère=char
        non_signé=unsigned signé=signed taille_de=sizeof principal=main vrai=true faux=false
        booléen=bool NUL=NULL classe=class publique=public privé=private protégé=protected
        nouveau=new supprimer=delete ceci=this espace_de_noms=namespace utilisant=using
        modèle=template nom_de_type=typename virtuel=virtual surcharge=override lancer=throw
        essayer=try attraper=catch amie=friend opérateur=operator
    """.trimIndent())

    val CPP_BIBLIO = parsePairs("""
        afficher=printf saisir=scanf longueur_chaîne=strlen copier_chaîne=strcpy comparer_chaîne=strcmp
        concaténer_chaîne=strcat racine_carrée=sqrt puissance=pow valeur_absolue=abs aléatoire=rand
        graine_aléatoire=srand quitter=exit allouer=malloc réallouer=realloc libérer=free
        ouvrir_fichier=fopen fermer_fichier=fclose lire_ligne=fgets écrire_fichier=fprintf temps=time
        sortie=cout entrée=cin fin_ligne=endl chaîne=string vecteur=vector carte=map ensemble=set
        ajouter_fin=push_back taille=size est_vide=empty commencer=begin terminer=end trier=sort
    """.trimIndent())

    // --- HTML ---
    val HTML_BALISES = parsePairs("""
        document=html tête=head corps=body titre=title titre1=h1 titre2=h2 titre3=h3 titre4=h4 titre5=h5 titre6=h6
        paragraphe=p lien=a image=img liste=ul liste-ordonnée=ol élément=li division=div portée=span
        saut-de-ligne=br ligne-horizontale=hr tableau=table ligne=tr cellule=td cellule-entête=th
        entête-tableau=thead corps-tableau=tbody pied-tableau=tfoot formulaire=form champ=input
        bouton=button étiquette=label zone-texte=textarea sélection=select option=option
        groupe-champs=fieldset légende-groupe=legend script=script style=style meta=meta ressource=link
        navigation=nav principal=main section=section article=article pied-de-page=footer entête=header
        côté=aside gras=strong italique=em code=code préformaté=pre citation=blockquote figure=figure
        légende=figcaption vidéo=video audio=audio source-média=source canevas=canvas cadre=iframe
        détails=details résumé=summary abréviation=abbr petit=small souligné=u barré=s marque=mark
        exposant=sup indice=sub temps=time liste-définitions=dl terme=dt description=dd modèle=template base=base
    """.trimIndent())

    val HTML_ATTRIBUTS = parsePairs("""
        classe=class identifiant=id source=src adresse=href texte-alternatif=alt titre=title langue=lang
        jeu-de-caractères=charset contenu=content nom=name valeur=value espace-réservé=placeholder
        largeur=width hauteur=height requis=required désactivé=disabled lecture-seule=readonly coché=checked
        sélectionné=selected action=action méthode=method pour=for cible=target relation=rel type=type
        style=style minimum=min maximum=max pas=step lignes=rows colonnes=cols contrôles=controls
        lecture-auto=autoplay boucle=loop différé=defer asynchrone=async au-clic=onclick au-survol=onmouseover
        au-changement=onchange au-envoi=onsubmit au-chargement=onload
    """.trimIndent())

    // --- CSS ---
    val CSS_PROPRIETES = parsePairs("""
        couleur=color couleur-de-fond=background-color fond=background image-de-fond=background-image
        répétition-de-fond=background-repeat taille-de-fond=background-size position-de-fond=background-position
        taille-de-police=font-size police=font-family graisse=font-weight style-de-police=font-style
        alignement-du-texte=text-align décoration-du-texte=text-decoration transformation-du-texte=text-transform
        hauteur-de-ligne=line-height espacement-des-lettres=letter-spacing ombre-du-texte=text-shadow
        marge=margin marge-haut=margin-top marge-bas=margin-bottom marge-gauche=margin-left marge-droite=margin-right
        remplissage=padding remplissage-haut=padding-top remplissage-bas=padding-bottom remplissage-gauche=padding-left
        remplissage-droite=padding-right bordure=border bordure-haut=border-top bordure-bas=border-bottom
        bordure-gauche=border-left bordure-droite=border-right rayon-de-bordure=border-radius
        couleur-de-bordure=border-color style-de-bordure=border-style largeur-de-bordure=border-width
        contour=outline largeur=width hauteur=height largeur-max=max-width largeur-min=min-width
        hauteur-max=max-height hauteur-min=min-height affichage=display position=position haut=top bas=bottom
        gauche=left droite=right débordement=overflow opacité=opacity ombre-de-boîte=box-shadow curseur=cursor
        transition=transition transformation=transform animation=animation visibilité=visibility index-z=z-index
        flottant=float dégagement=clear dimensionnement=box-sizing filtre=filter direction-flex=flex-direction
        justification=justify-content alignement-éléments=align-items alignement-contenu=align-content
        retour-flex=flex-wrap espacement=gap flex=flex colonnes-grille=grid-template-columns
        lignes-grille=grid-template-rows contenu=content style-de-liste=list-style alignement-vertical=vertical-align
    """.trimIndent())

    val CSS_VALEURS = parsePairs("""
        rouge=red bleu=blue vert=green jaune=yellow noir=black blanc=white gris=gray orange=orange
        violet=purple rose=pink marron=brown turquoise=cyan or=gold argent=silver gras=bold italique=italic
        centre=center gauche=left droite=right justifié=justify souligné=underline barré=line-through
        aucun=none bloc=block en-ligne=inline bloc-en-ligne=inline-block grille=grid absolu=absolute
        relatif=relative fixe=fixed collant=sticky caché=hidden défiler=scroll plein=solid pointillé=dotted
        tirets=dashed pointeur=pointer colonne=column ligne=row espace-entre=space-between
        espace-autour=space-around début-flex=flex-start fin-flex=flex-end étiré=stretch majuscule=uppercase
        minuscule=lowercase haut=top bas=bottom milieu=middle hérité=inherit initial=initial
    """.trimIndent())

    val CSS_FONCTIONS = parsePairs("""
        dégradé-linéaire=linear-gradient dégradé-radial=radial-gradient calculer=calc variable=var
        limiter=clamp répéter=repeat translater=translate tourner=rotate échelle=scale incliner=skew
    """.trimIndent())

    val CSS_PSEUDO = parsePairs("""
        survol=hover actif=active focus=focus premier-enfant=first-child dernier-enfant=last-child
        enfant-numéro=nth-child avant=before après=after non=not vide=empty visité=visited
        première-lettre=first-letter première-ligne=first-line sélection=selection
    """.trimIndent())

    val CSS_REGLES = parsePairs("""
        média=media images-clés=keyframes police=font-face importer=import
    """.trimIndent())
}
