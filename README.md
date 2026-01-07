# Projet Sudoku - Application Interactive ScalaFX

![Présentation du projet](IED-M1-Page_de_présentation-Maxime-BRONNY.png)

## Informations generales

| Champ | Valeur |
|-------|--------|
| **Projet** | Sudoku Interactif avec Interface Graphique |
| **Auteur** | Maxime BRONNY |
| **Formation** | Master 1 Informatique Big Data |
| **UE** | Programmation Concurrente |
| **Version** | 1.0 |
| **Date** | Janvier 2026 |

---

## Description du projet

Ce projet consiste en une implementation complete d'un jeu de Sudoku avec interface graphique, developpee en Scala avec le framework ScalaFX. L'application permet de jouer a des puzzles de Sudoku generes aleatoirement avec garantie de solution unique, tout en offrant des fonctionnalites avancees telles que la validation en temps reel, le systeme d'annulation/retablissement, et la persistance des parties.

### Technologies utilisees

| Technologie | Version | Role |
|-------------|---------|------|
| Scala | 2.13.12 | Langage de programmation principal |
| ScalaFX | 21.0.0-R32 | Framework d'interface graphique |
| JavaFX | 21 | Backend graphique (OpenJFX) |
| uPickle | 3.1.3 | Serialisation JSON |
| ScalaTest | 3.2.17 | Framework de tests unitaires |
| Scala-CLI | - | Outil de build et execution |

### Paradigmes de programmation

- **Programmation fonctionnelle** : utilisation extensive de structures immutables, fonctions pures, et pattern matching
- **Architecture MVC** : separation claire entre modele de donnees, logique metier et interface utilisateur
- **Programmation reactive** : utilisation des Property ScalaFX pour la synchronisation automatique entre modele et vue

---

## Fonctionnalites principales

### Generation de puzzles
- Generation aleatoire de grilles Sudoku valides
- Garantie de solution unique via verification par backtracking
- Trois niveaux de difficulte : Facile (35-40 cases vides), Moyen (45-50), Difficile (55-60)

### Interface de jeu
- Grille interactive 9x9 avec selection au clic ou au clavier
- Saisie des valeurs via pave numerique graphique ou clavier (1-9)
- Navigation dans la grille avec les touches directionnelles

### Validation et feedback visuel
- Verification en temps reel de la validite des coups
- Affichage en rouge des valeurs incorrectes
- Affichage en vert des valeurs correctes
- Mise en surbrillance des cellules en conflit (contraintes violees)

### Systeme Undo/Redo
- Annulation illimitee des actions (bouton "Annuler")
- Retablissement des actions annulees (bouton "Refaire")
- Historique persistant dans les sauvegardes

### Persistance des donnees
- Sauvegarde de l'etat complet de la partie au format JSON
- Chargement de parties sauvegardees
- Catalogue de puzzles pre-generes

### Mode aide
- Mise en surbrillance de toutes les cellules contenant une valeur selectionnee
- Limite a une utilisation par partie (compteur affiche)

### Affichage des contraintes
- Mode "Contraintes" pour visualiser les cellules liees (meme ligne, colonne, bloc 3x3)
- Aide a la comprehension des regles du Sudoku

### Statistiques de jeu
- Chronometre avec affichage en temps reel
- Compteur d'erreurs commises
- Affichage du niveau de difficulte

---

## Architecture du projet

```
[schema a generer via draw.io ou Mermaid]

Architecture en couches de type MVC :

+--------------------------------------------------+
|                      UI Layer                     |
|  MainApp, GameView, SudokuGridPane, CellView,    |
|  ToolbarView, StatsPanel, PuzzleSelector, Theme  |
+--------------------------------------------------+
                        |
                        v
+--------------------------------------------------+
|                   Logic Layer                     |
|         Validator, Solver, Generator             |
+--------------------------------------------------+
                        |
                        v
+--------------------------------------------------+
|                   Model Layer                     |
|     Cell, SudokuGrid, GameState, Action          |
+--------------------------------------------------+
                        |
                        v
+--------------------------------------------------+
|               Persistence Layer                   |
|        GameRepository, PuzzleCatalog             |
+--------------------------------------------------+
                        |
                        v
+--------------------------------------------------+
|                  File System                      |
|            data/, saves/ (JSON files)            |
+--------------------------------------------------+
```

---

## Structure des fichiers

```
PROJET_SUDOKU/
|
+-- src/
|   +-- main/scala/sudoku/
|   |   +-- Main.scala                      # Point d'entree de l'application
|   |   |
|   |   +-- model/                          # Modeles de donnees immutables
|   |   |   +-- Cell.scala                  # Cellule individuelle (valeur, fixe)
|   |   |   +-- SudokuGrid.scala            # Grille 9x9 immutable
|   |   |   +-- Action.scala                # Actions pour Undo/Redo
|   |   |   +-- GameState.scala             # Etat complet d'une partie
|   |   |
|   |   +-- logic/                          # Logique metier
|   |   |   +-- Validator.scala             # Validation des regles Sudoku
|   |   |   +-- Solver.scala                # Solveur par backtracking
|   |   |   +-- Generator.scala             # Generateur de puzzles
|   |   |
|   |   +-- persistence/                    # Couche de persistance
|   |   |   +-- GameRepository.scala        # Sauvegarde/Chargement JSON
|   |   |   +-- PuzzleCatalog.scala         # Gestion du catalogue
|   |   |
|   |   +-- ui/                             # Interface graphique ScalaFX
|   |       +-- MainApp.scala               # Application principale
|   |       +-- Theme.scala                 # Theme visuel "Warm Modern"
|   |       +-- GameView.scala              # Vue principale du jeu
|   |       +-- SudokuGridPane.scala        # Conteneur de la grille
|   |       +-- CellView.scala              # Vue d'une cellule
|   |       +-- ToolbarView.scala           # Barre d'outils laterale
|   |       +-- StatsPanel.scala            # Panneau de statistiques
|   |       +-- PuzzleSelector.scala        # Selecteur de puzzles
|   |
|   +-- test/scala/sudoku/                  # Tests unitaires
|   |   +-- ValidatorSpec.scala             # Tests du validateur
|   |   +-- SolverSpec.scala                # Tests du solveur
|   |   +-- GeneratorSpec.scala             # Tests du generateur
|   |   +-- PersistenceSpec.scala           # Tests de persistance
|   |
|   +-- project.scala                       # Configuration Scala-CLI
|
+-- data/                                   # Donnees de l'application
|   +-- catalog.json                        # Catalogue de puzzles
|
+-- saves/                                  # Sauvegardes utilisateur
|   +-- *.json                              # Fichiers de sauvegarde
|
+-- REQUIREMENTS.txt                        # Prerequis systeme
+-- README.md                               # Ce fichier
+-- build.sbt                               # Configuration SBT (alternative)
```

---

## Algorithmes implementes

### Solveur par backtracking avec heuristique MRV

L'algorithme de resolution utilise la technique du backtracking avec l'heuristique MRV (Minimum Remaining Values) pour optimiser la recherche.

```
[schema a generer via Mermaid - flowchart]

Algorithme solve(grille):
    |
    v
[Trouver cellule vide avec minimum de candidats (MRV)]
    |
    +-- Aucune cellule vide --> [SOLUTION TROUVEE]
    |
    v
[Pour chaque candidat valide (1-9)]
    |
    v
[Placer candidat dans cellule]
    |
    v
[Appel recursif solve(grille)]
    |
    +-- Succes --> [Retourner solution]
    |
    v
[Backtrack: retirer valeur]
    |
    v
[Essayer candidat suivant]
    |
    +-- Plus de candidats --> [ECHEC, remonter]
```

**Complexite** : O(9^n) dans le pire cas, ou n est le nombre de cellules vides. L'heuristique MRV reduit significativement l'espace de recherche en pratique.

### Generateur de puzzles avec solution unique

```
[schema a generer via Mermaid - flowchart]

Algorithme generate(difficulte):
    |
    v
[Remplir blocs diagonaux 3x3 aleatoirement]
    |
    v
[Resoudre grille par backtracking]
    |
    v
[Melanger liste des 81 positions]
    |
    v
[Pour chaque position (jusqu'a objectif atteint)]
    |
    v
[Sauvegarder valeur, vider cellule]
    |
    v
[Compter solutions (max 2)]
    |
    +-- 1 solution --> [Conserver cellule vide]
    |
    +-- >1 solutions --> [Restaurer valeur]
    |
    v
[Retourner puzzle avec cellules fixes marquees]
```

**Garantie** : L'algorithme assure qu'il existe exactement une solution en verifiant apres chaque suppression.

---

## Diagramme de classes

```
[schema a generer via PlantUML ou draw.io]

Package model:
+----------------+     +------------------+
|     Cell       |     |   SudokuGrid     |
+----------------+     +------------------+
| - value: Int   |     | - cells: Vector  |
| - fixed: Bool  |<----| + get(r,c): Cell |
+----------------+     | + set(r,c,v)     |
                       | + isComplete()   |
                       +------------------+
                              ^
                              |
+----------------+     +------------------+
|    Action      |     |   GameState      |
+----------------+     +------------------+
| <<sealed>>     |     | - grid           |
| + row: Int     |---->| - solution       |
| + col: Int     |     | - history        |
+----------------+     | - errorCount     |
        ^              | + setValue()     |
        |              | + undo()/redo()  |
+-------+-------+      +------------------+
|               |
+-----------+   +-----------+
|SetValue   |   |ClearAction|
|Action     |   |           |
+-----------+   +-----------+

Package logic:
+----------------+  +----------------+  +----------------+
|   Validator    |  |    Solver      |  |   Generator    |
+----------------+  +----------------+  +----------------+
| + isMoveValid()|  | + solve()      |  | + generate()   |
| + getViolations|  | + countSolutions| | - fillBlock()  |
| + getAllConflicts| | + hasUnique()  |  | - createPuzzle()|
+----------------+  +----------------+  +----------------+

Package ui:
+----------------+     +------------------+
|    MainApp     |---->|    GameView      |
+----------------+     +------------------+
                       | - gridPane       |
                       | - toolbar        |
                       | - statsPanel     |
                       | + startNewGame() |
                       +------------------+
                              |
        +---------------------+---------------------+
        v                     v                     v
+----------------+  +------------------+  +----------------+
|SudokuGridPane  |  |   ToolbarView    |  |  StatsPanel    |
+----------------+  +------------------+  +----------------+
| - cells[9][9]  |  | + newGameBtn     |  | + timeLabel    |
| + updateFromGrid| | + saveBtn        |  | + errorLabel   |
| + showConflicts |  | + undoBtn       |  | + updateStats()|
+----------------+  +------------------+  +----------------+
        |
        v
+----------------+
|   CellView     |
+----------------+
| - valueProperty|
| - fixedProperty|
| - errorProperty|
| + updateStyle()|
+----------------+
```

---

## Flux de donnees

```
[schema a generer via Mermaid - sequence diagram]

Interaction utilisateur - Placement d'une valeur:

User          CellView       GameView       GameState      Validator
  |               |              |              |              |
  |---(clic)----->|              |              |              |
  |               |---(event)--->|              |              |
  |               |              |--setValue()-->|              |
  |               |              |              |---(backup)--->|
  |               |              |              |<---(check)----|
  |               |              |<--(newState)--|              |
  |               |              |              |              |
  |               |              |---validate-->|              |
  |               |              |              |              |
  |               |<---(update)--|              |              |
  |<--(feedback)--|              |              |              |

Notes:
- GameState est immutable: setValue() retourne un nouvel objet
- Les Property ScalaFX propagent automatiquement les changements
- La validation est effectuee apres chaque modification
```

---

## Prerequis et installation

Les prerequis systeme sont detailles dans le fichier `REQUIREMENTS.txt`. En resume :

### Prerequis
- Java JDK 17 ou superieur
- Scala-CLI (derniere version stable)

### Installation rapide

```bash
# Verifier Java
java -version  # Doit afficher 17+

# Installer Scala-CLI (Linux/macOS)
curl -sSLf https://scala-cli.virtuslab.org/get | sh

# Verifier Scala-CLI
scala-cli version
```

### Commandes principales

```bash
# Compilation
scala-cli compile src/

# Execution
scala-cli run src/ --main-class sudoku.ui.MainApp

# Tests
scala-cli test src/
```

---

## Utilisation

### Controles clavier

| Touche | Action |
|--------|--------|
| 1-9 | Entrer une valeur dans la cellule selectionnee |
| Suppr / Backspace | Effacer la valeur de la cellule selectionnee |
| Fleches directionnelles | Naviguer dans la grille |
| Ctrl+Z | Annuler la derniere action |
| Ctrl+Y | Retablir l'action annulee |

### Controles souris

- **Clic sur cellule** : Selectionner la cellule
- **Clic sur pave numerique** : Entrer la valeur dans la cellule selectionnee

### Boutons de la barre d'outils

| Bouton | Fonction |
|--------|----------|
| Nouveau | Demarrer une nouvelle partie (choix de difficulte) |
| Catalogue | Parcourir et selectionner un puzzle existant |
| Charger | Charger une partie sauvegardee |
| Sauvegarder | Sauvegarder la partie en cours |
| Annuler | Annuler la derniere action (Undo) |
| Refaire | Retablir l'action annulee (Redo) |
| Effacer | Effacer la valeur de la cellule selectionnee |
| Aide (1/1) | Activer le mode de surbrillance des valeurs identiques |
| Contraintes | Afficher les cellules liees a la selection |

### Theme visuel

L'application utilise un theme "Warm Modern" avec les caracteristiques suivantes :
- Fond beige clair (#FFF7F2)
- Couleur principale brun fonce (#3B2F2F)
- Accent orange corail (#FF7A59)
- Feedback visuel : rouge pour erreurs, vert pour valeurs correctes

---

## Tests

### Suites de tests

| Fichier | Couverture |
|---------|------------|
| ValidatorSpec.scala | Validation des regles Sudoku, detection des conflits |
| SolverSpec.scala | Resolution de grilles, comptage de solutions |
| GeneratorSpec.scala | Generation de puzzles, unicite des solutions |
| PersistenceSpec.scala | Sauvegarde/Chargement JSON, integrite des donnees |

### Execution des tests

```bash
# Lancer tous les tests
scala-cli test src/

# Lancer un test specifique
scala-cli test src/ -- -o "ValidatorSpec"
```

### Couverture

Les tests couvrent :
- Les cas nominaux (grilles valides, solutions uniques)
- Les cas limites (grilles vides, grilles completes)
- Les cas d'erreur (violations de contraintes, donnees corrompues)

---

## Limitations connues

### Limitations actuelles
- La generation de puzzles difficiles peut prendre quelques secondes
- L'aide est limitee a une utilisation par partie
- Pas de mode multijoueur

### Ameliorations futures envisageables

1. **Performance** : Parallelisation de la generation de puzzles via Futures ou Actors
2. **Fonctionnalites** : 
   - Niveaux de difficulte supplementaires (Expert, Extreme)
   - Mode multijoueur en reseau
   - Statistiques globales et classements
3. **Interface** : 
   - Themes personnalisables
   - Animations de transition
   - Support tactile

---

## References

### Documentation technique
- [Scala Documentation](https://docs.scala-lang.org/)
- [ScalaFX Documentation](http://www.scalafx.org/)
- [JavaFX API](https://openjfx.io/javadoc/21/)

### Algorithmes
- Knuth, D. E. "Dancing Links" - Algorithme X pour la resolution de Sudoku
- Russell, S. & Norvig, P. "Artificial Intelligence: A Modern Approach" - Backtracking et CSP

---

## Licence

Projet realise dans le cadre du cours de Programmation Concurrente, Master 1 Informatique Big Data.

