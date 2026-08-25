# Event Master 1.2.8

Mod NeoForge 1.21.1 cote serveur. Les joueurs n'ont pas besoin d'installer le mod.

## Fonction actuelle

- `/event` ouvre le panneau de contrôle aux opérateurs (permission 2).
- `Shared inventory` synchronise les 41 emplacements de l'inventaire entre tous les joueurs connectes.
- `Shared health` partage la valeur de vie la plus basse entre tous les joueurs.
- `Montée de lave` ouvre un écran de réglages avec 5, 10, 15, 20, 25 ou 30 minutes de préparation.
- La vitesse peut être réglée sur x1, x2 ou x3, puis l'événement démarre avec le bouton `Démarrer`.
- Une boss bar affiche le compte à rebours et le niveau actuel de la lave.
- La lave commence au fond du monde et monte progressivement autour des joueurs.
- La zone couvre un rayon de 100 chunks autour de chaque joueur et une bordure en blocs barrière suit la hauteur de la lave.
- Pour limiter le lag, seuls les chunks déjà chargés sont traités et au maximum deux chunks sont remplis par tick.
- La vitesse x1, x2 ou x3 peut être changée dans le menu pendant que l'événement est déjà en cours.
- `Death Swap` ouvre un écran avec des échanges toutes les 1, 5, 10 ou 15 minutes.
- Le compteur Death Swap peut être affiché ou caché avec l'option `Temps en haut`.
- Au moment de l'échange, les participants changent de position. Si un participant meurt, les survivants gagnent.
- `Effets aléatoires` permet de choisir un joueur et d'appliquer un effet aléatoire toutes les 10, 30 ou 60 secondes.
- Chaque effet dure exactement l'intervalle choisi avant d'être remplacé par le suivant.
- `Vitesse du jeu` propose x1, x2, x4, x6, x8, x10, x20 et x50 pour accélérer les déplacements et attaques des créatures.
- `Barre effets` permet de cacher ou réafficher le compteur des effets aléatoires.
- Les transitions entre les panneaux ferment proprement l'écran précédent pour éviter les boutons bloqués.
- L'ouverture des panneaux est différée d'un tick serveur afin d'éviter les conflits de clics entre deux menus.
- `Voir l'inventaire d'un joueur`, placé à gauche de `Inventaire partagé`, permet de choisir un joueur et de prendre ou déposer des objets dans son inventaire.
- L'écran montre séparément l'inventaire principal, la barre rapide, les armures et la main secondaire du joueur ciblé, ainsi que l'inventaire de l'administrateur.
- Les couleurs et séparateurs de l'écran distinguent clairement l'inventaire ciblé et celui de l'administrateur.
- La vitesse globale accélère aussi les ticks naturels du monde, comme la pousse et la propagation du feu.
- `/event stop` arrête tous les événements, `/event status` affiche leur état, `/event history` affiche l'historique, et `/event reset` réinitialise tout.
- Les démarrages, échanges et fins importantes utilisent des titres et des sons vanilla.
- `Bordure évolutive` utilise la bordure vanilla : jour 1 = 1x1 bloc, jour 2 = 2x2, jour 3 = 4x4, puis la taille double chaque jour.
- La bordure est centrée sur la position de l'opérateur qui la démarre et son rendu est géré par Minecraft, sans remplissage massif de blocs.
- Les options restent actives jusqu'a l'arret du serveur.

## Compiler

1. Installer Java 21 et Gradle 8.8 ou plus recent.
2. Ouvrir ce dossier dans un terminal.
3. Executer `gradle build`.
4. Copier `build/libs/eventcontrol-1.2.8.jar` dans le dossier `mods` du serveur NeoForge 1.21.1.

Le client se connecte avec Minecraft vanilla compatible avec le serveur ; aucun fichier du mod n'est requis cote client.
"# eventcontrol" 
