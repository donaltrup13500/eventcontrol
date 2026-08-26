# Event Master 1.3.4

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
- `Vitesse du jeu` propose x1, x2, x4, x6, x8, x10, x20 et x50. Elle modifie le vrai taux de ticks du serveur pour accélérer la simulation complète : déplacements, IA, redstone, fluides, croissance et événements temporisés.
- `Barre effets` permet de cacher ou réafficher le compteur des effets aléatoires.
- Les countdowns sont affichés dans un HUD personnalisé compact à gauche de l'écran, avec une barre de progression et le temps restant pour la lave, le Death Swap, les effets et l'arrivée du Virus. Les boss bars vanilla des countdowns sont désactivées. La santé du Virus garde son propre panneau personnalisé compact.
- Les transitions entre les panneaux ferment proprement l'écran précédent pour éviter les boutons bloqués.
- L'ouverture des panneaux est différée d'un tick serveur afin d'éviter les conflits de clics entre deux menus.
- `Voir l'inventaire d'un joueur`, placé à gauche de `Inventaire partagé`, permet de choisir un joueur et de prendre ou déposer des objets dans son inventaire.
- L'écran montre séparément l'inventaire principal, la barre rapide, les armures et la main secondaire du joueur ciblé, ainsi que l'inventaire de l'administrateur.
- Les couleurs et séparateurs de l'écran distinguent clairement l'inventaire ciblé et celui de l'administrateur.
- La vitesse globale accélère aussi les ticks naturels du monde, comme la pousse et la propagation du feu.
- `/event stop` arrête tous les événements, `/event status` affiche leur état, `/event history` affiche l'historique, et `/event reset` réinitialise tout.
- Les démarrages, échanges et fins importantes utilisent des titres et des sons vanilla.
- `Bordure évolutive` utilise la bordure vanilla : jour 1 = 1x1 bloc, jour 2 = 2x2, jour 3 = 4x4, puis la taille double chaque jour.
- `Virus` se lance depuis le bouton du menu principal. Il possède 4 niveaux : niveau 1 lent et difficile à faire miner, niveau 2 plus rapide avec armure en fer, niveau 3 très rapide avec armure en diamant, puis niveau 4 hardcore avec armure en netherite, très grande résistance et destruction rapide des blocs. Il évolue après chaque mort et reste au niveau 4 ensuite.
- `/event jumpdeath` active l'événement où le changement sol -> saut fait exploser et tuer le joueur.
- Le menu principal fournit des boutons séparés `ON` et `OFF` pour les événements activables, afin d'éviter les bascules involontaires.
- `Soleil dangereux` inflige des dégâts de feu aux joueurs exposés au soleil pendant la journée. Il faut placer un bloc au-dessus de sa tête pour être protégé. Il peut être activé avec le bouton du menu ou `/event sun`.
- La version 1.3.4 apporte une refonte graphique des menus avec une interface personnalisée, des cadres haute définition et un bouton pour masquer ou afficher la barre du Virus.
- La bordure est centrée sur la position de l'opérateur qui la démarre et son rendu est géré par Minecraft, sans remplissage massif de blocs.
- Les options restent actives jusqu'a l'arret du serveur.

## Compiler

1. Installer Java 21 et Gradle 8.8 ou plus recent.
2. Ouvrir ce dossier dans un terminal.
3. Executer `gradle build`.
4. Copier `build/libs/eventcontrol-1.2.9.jar` dans le dossier `mods` du serveur NeoForge 1.21.1.

Le client se connecte avec Minecraft vanilla compatible avec le serveur ; aucun fichier du mod n'est requis cote client.
"# eventcontrol" 
