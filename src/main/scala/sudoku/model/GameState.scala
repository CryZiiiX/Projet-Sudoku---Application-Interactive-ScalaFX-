/*
 * Nom     : src/main/scala/sudoku/model/GameState.scala
 * Role    : Gestion de l'etat complet d'une partie de Sudoku.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.model

import java.time.Instant
import java.util.UUID

/**
 * Trait representant les niveaux de difficulte.
 */
sealed trait Difficulty {
  def name: String
  def emptyCells: (Int, Int)
}

/**
 * Objet compagnon pour les niveaux de difficulte.
 */
object Difficulty {
  
  /**
   * Niveau Facile : 35-40 cases vides.
   */
  case object Easy extends Difficulty {
    val name = "Facile"
    val emptyCells = (35, 40)
  }
  
  /**
   * Niveau Moyen : 45-50 cases vides.
   */
  case object Medium extends Difficulty {
    val name = "Moyen"
    val emptyCells = (45, 50)
  }
  
  /**
   * Niveau Difficile : 55-60 cases vides.
   */
  case object Hard extends Difficulty {
    val name = "Difficile"
    val emptyCells = (55, 60)
  }
  
  /**
   * Fonction : fromString
   * Role     : Convertit une chaine en niveau de difficulte.
   * Param    : s - nom du niveau.
   * Retour   : Difficulty correspondant.
   */
  def fromString(s: String): Difficulty = s.toLowerCase match {
    case "easy" | "facile" => Easy
    case "medium" | "moyen" => Medium
    case "hard" | "difficile" => Hard
    case _ => Medium
  }
  
  /**
   * Liste de tous les niveaux de difficulte.
   */
  val all: List[Difficulty] = List(Easy, Medium, Hard)
}

/**
 * Fonction : GameState
 * Role     : Represente l'etat complet d'une partie de Sudoku.
 * Param    : id - identifiant unique de la partie.
 * Param    : puzzleId - identifiant du puzzle.
 * Param    : grid - grille actuelle du jeu.
 * Param    : solution - grille solution.
 * Param    : difficulty - niveau de difficulte.
 * Param    : startTime - heure de debut de la partie.
 * Param    : elapsedSeconds - temps ecoule en secondes.
 * Param    : errorCount - nombre d'erreurs commises.
 * Param    : history - historique des actions.
 * Param    : completed - true si la partie est terminee.
 * Param    : helpUsed - true si l'aide a ete utilisee.
 */
case class GameState(
  id: String,
  puzzleId: String,
  grid: SudokuGrid,
  solution: SudokuGrid,
  difficulty: Difficulty,
  startTime: Instant,
  elapsedSeconds: Long,
  errorCount: Int,
  history: ActionHistory,
  completed: Boolean,
  helpUsed: Boolean = false
) {
  
  /**
   * Fonction : useHelp
   * Role     : Marque l'aide comme utilisee.
   * Retour   : GameState avec helpUsed a true.
   */
  def useHelp(): GameState = copy(helpUsed = true)
  
  /**
   * Fonction : isHelpAvailable
   * Role     : Verifie si l'aide est encore disponible.
   * Retour   : true si l'aide n'a pas ete utilisee.
   */
  def isHelpAvailable: Boolean = !helpUsed
  
  /**
   * Fonction : setValue
   * Role     : Place une valeur dans une cellule et enregistre l'action.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : value - valeur a placer.
   * Retour   : GameState mis a jour.
   */
  def setValue(row: Int, col: Int, value: Int): GameState = {
    if (grid.isFixed(row, col)) return this
    
    val oldValue = grid.getValue(row, col)
    if (oldValue == value) return this
    
    val action = SetValueAction(row, col, oldValue, value)
    history.push(action)
    
    val newGrid = grid.set(row, col, value)
    val isError = value != 0 && solution.getValue(row, col) != value
    val newErrorCount = if (isError) errorCount + 1 else errorCount
    val isCompleted = newGrid.isComplete && isGridCorrect(newGrid)
    
    copy(
      grid = newGrid,
      errorCount = newErrorCount,
      completed = isCompleted
    )
  }
  
  /**
   * Fonction : clearCell
   * Role     : Efface la valeur d'une cellule et enregistre l'action.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : GameState mis a jour.
   */
  def clearCell(row: Int, col: Int): GameState = {
    if (grid.isFixed(row, col)) return this
    
    val oldValue = grid.getValue(row, col)
    if (oldValue == 0) return this
    
    val action = ClearAction(row, col, oldValue)
    history.push(action)
    
    copy(grid = grid.clear(row, col))
  }
  
  /**
   * Fonction : undo
   * Role     : Annule la derniere action effectuee.
   * Retour   : GameState avec l'action annulee.
   */
  def undo(): GameState = {
    history.undo() match {
      case Some(SetValueAction(r, c, oldVal, _)) =>
        copy(grid = grid.set(r, c, oldVal))
      case Some(ClearAction(r, c, oldVal)) =>
        copy(grid = grid.set(r, c, oldVal))
      case None => this
    }
  }
  
  /**
   * Fonction : redo
   * Role     : Retablit la derniere action annulee.
   * Retour   : GameState avec l'action retablie.
   */
  def redo(): GameState = {
    history.redo() match {
      case Some(SetValueAction(r, c, _, newVal)) =>
        copy(grid = grid.set(r, c, newVal))
      case Some(ClearAction(r, c, _)) =>
        copy(grid = grid.clear(r, c))
      case None => this
    }
  }
  
  /**
   * Fonction : canUndo
   * Role     : Verifie si une annulation est possible.
   * Retour   : true si l'historique contient des actions.
   */
  def canUndo: Boolean = history.canUndo
  
  /**
   * Fonction : canRedo
   * Role     : Verifie si un retablissement est possible.
   * Retour   : true si des actions annulees existent.
   */
  def canRedo: Boolean = history.canRedo
  
  /**
   * Fonction : updateElapsed
   * Role     : Met a jour le temps ecoule.
   * Param    : seconds - nouveau temps en secondes.
   * Retour   : GameState avec le temps mis a jour.
   */
  def updateElapsed(seconds: Long): GameState = copy(elapsedSeconds = seconds)
  
  /**
   * Fonction : isGridCorrect
   * Role     : Verifie si la grille correspond a la solution.
   * Param    : g - grille a verifier.
   * Retour   : true si toutes les valeurs correspondent.
   */
  private def isGridCorrect(g: SudokuGrid): Boolean = {
    (0 until 9).forall { r =>
      (0 until 9).forall { c =>
        g.getValue(r, c) == solution.getValue(r, c)
      }
    }
  }
  
  /**
   * Fonction : formattedTime
   * Role     : Formate le temps ecoule pour affichage.
   * Retour   : Chaine formatee MM:SS.
   */
  def formattedTime: String = {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    f"$minutes%02d:$seconds%02d"
  }
}

/**
 * Objet compagnon pour la creation de GameState.
 */
object GameState {
  
  /**
   * Fonction : create
   * Role     : Cree un nouvel etat de partie.
   * Param    : puzzleId - identifiant du puzzle.
   * Param    : puzzle - grille initiale.
   * Param    : solution - grille solution.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : GameState initialise.
   */
  def create(
    puzzleId: String,
    puzzle: SudokuGrid,
    solution: SudokuGrid,
    difficulty: Difficulty
  ): GameState = {
    GameState(
      id = UUID.randomUUID().toString,
      puzzleId = puzzleId,
      grid = puzzle,
      solution = solution,
      difficulty = difficulty,
      startTime = Instant.now(),
      elapsedSeconds = 0,
      errorCount = 0,
      history = ActionHistory(),
      completed = false,
      helpUsed = false
    )
  }
}
