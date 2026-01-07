/*
 * Nom     : src/main/scala/sudoku/persistence/GameRepository.scala
 * Role    : Sauvegarde et chargement des parties en fichiers JSON.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.persistence

import sudoku.model._
import upickle.default._
import java.io.{File, PrintWriter}
import java.time.Instant
import scala.io.Source
import scala.util.{Try, Using}

/**
 * Fonction : ActionJson
 * Role     : Representation JSON d'une action pour serialisation.
 * Param    : actionType - type d'action (SET ou CLEAR).
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : oldValue - ancienne valeur.
 * Param    : newValue - nouvelle valeur.
 */
case class ActionJson(
  actionType: String,
  row: Int,
  col: Int,
  oldValue: Int,
  newValue: Int
)

/**
 * Fonction : GameStateJson
 * Role     : Representation JSON complete d'un etat de partie.
 * Param    : id - identifiant de la partie.
 * Param    : puzzleId - identifiant du puzzle.
 * Param    : grid - grille actuelle.
 * Param    : fixedCells - positions des cellules fixes.
 * Param    : solution - grille solution.
 * Param    : difficulty - niveau de difficulte.
 * Param    : elapsedSeconds - temps ecoule.
 * Param    : errorCount - nombre d'erreurs.
 * Param    : history - historique des actions.
 * Param    : historyIndex - position dans l'historique.
 * Param    : savedAt - date de sauvegarde.
 * Param    : helpUsed - etat de l'aide.
 */
case class GameStateJson(
  id: String,
  puzzleId: String,
  grid: Seq[Seq[Int]],
  fixedCells: Seq[Seq[Int]],
  solution: Seq[Seq[Int]],
  difficulty: String,
  elapsedSeconds: Long,
  errorCount: Int,
  history: Seq[ActionJson],
  historyIndex: Int,
  savedAt: String,
  helpUsed: Boolean = false
)

/**
 * Objet de gestion de la persistance des parties.
 */
object GameRepository {
  
  implicit val actionJsonRw: ReadWriter[ActionJson] = macroRW
  implicit val gameStateJsonRw: ReadWriter[GameStateJson] = macroRW
  
  val savesDirectory: String = "saves"
  
  /**
   * Fonction : save
   * Role     : Sauvegarde un etat de jeu dans un fichier JSON.
   * Param    : state - etat de la partie.
   * Param    : path - chemin du fichier de sauvegarde.
   * Retour   : true si la sauvegarde a reussi.
   */
  def save(state: GameState, path: String): Boolean = {
    try {
      new File(savesDirectory).mkdirs()
      
      val json = toJson(state)
      val jsonString = write(json, indent = 2)
      
      Using(new PrintWriter(new File(path))) { writer =>
        writer.write(jsonString)
      }
      true
    } catch {
      case e: Exception =>
        println(s"Erreur sauvegarde: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Fonction : load
   * Role     : Charge un etat de jeu depuis un fichier JSON.
   * Param    : path - chemin du fichier a charger.
   * Retour   : Option contenant l'etat charge.
   */
  def load(path: String): Option[GameState] = {
    try {
      val source = Source.fromFile(path)
      val content = try source.mkString finally source.close()
      val json = read[GameStateJson](content)
      Some(fromJson(json))
    } catch {
      case e: Exception =>
        println(s"Erreur chargement: ${e.getMessage}")
        None
    }
  }
  
  /**
   * Fonction : listSaves
   * Role     : Liste tous les fichiers de sauvegarde disponibles.
   * Retour   : Sequence des chemins des sauvegardes.
   */
  def listSaves(): Seq[String] = {
    val dir = new File(savesDirectory)
    if (dir.exists() && dir.isDirectory) {
      dir.listFiles()
        .filter(_.getName.endsWith(".json"))
        .map(_.getAbsolutePath)
        .toSeq
        .sorted
        .reverse
    } else {
      Seq.empty
    }
  }
  
  /**
   * Fonction : toJson
   * Role     : Convertit un GameState en representation JSON.
   * Param    : state - etat a convertir.
   * Retour   : GameStateJson.
   */
  private def toJson(state: GameState): GameStateJson = {
    val gridSeq = (0 until 9).map { r =>
      (0 until 9).map(c => state.grid.getValue(r, c))
    }
    
    val fixedSeq = state.grid.fixedPositions.toSeq.map { case (r, c) => Seq(r, c) }
    
    val solutionSeq = (0 until 9).map { r =>
      (0 until 9).map(c => state.solution.getValue(r, c))
    }
    
    val historySeq = state.history.toList.map {
      case SetValueAction(r, c, old, newVal) =>
        ActionJson("SET", r, c, old, newVal)
      case ClearAction(r, c, old) =>
        ActionJson("CLEAR", r, c, old, 0)
    }
    
    GameStateJson(
      id = state.id,
      puzzleId = state.puzzleId,
      grid = gridSeq,
      fixedCells = fixedSeq,
      solution = solutionSeq,
      difficulty = state.difficulty.name,
      elapsedSeconds = state.elapsedSeconds,
      errorCount = state.errorCount,
      history = historySeq,
      historyIndex = state.history.undoCount,
      savedAt = Instant.now().toString,
      helpUsed = state.helpUsed
    )
  }
  
  /**
   * Fonction : fromJson
   * Role     : Convertit une representation JSON en GameState.
   * Param    : json - representation JSON.
   * Retour   : GameState reconstitue.
   */
  private def fromJson(json: GameStateJson): GameState = {
    val gridArray = json.grid.map(_.toArray).toArray
    val fixedSet = json.fixedCells.map(pos => (pos(0), pos(1))).toSet
    val grid = SudokuGrid.fromArrayWithFixed(gridArray, fixedSet)
    
    val solutionArray = json.solution.map(_.toArray).toArray
    val solution = SudokuGrid.fromArray(solutionArray, allFixed = false)
    
    val history = ActionHistory()
    val actions = json.history.map {
      case ActionJson("SET", r, c, old, newVal) =>
        SetValueAction(r, c, old, newVal)
      case ActionJson("CLEAR", r, c, old, _) =>
        ClearAction(r, c, old)
      case ActionJson(t, r, c, old, newVal) =>
        SetValueAction(r, c, old, newVal)
    }.toList
    history.loadFromList(actions, json.historyIndex)
    
    GameState(
      id = json.id,
      puzzleId = json.puzzleId,
      grid = grid,
      solution = solution,
      difficulty = Difficulty.fromString(json.difficulty),
      startTime = Instant.now(),
      elapsedSeconds = json.elapsedSeconds,
      errorCount = json.errorCount,
      history = history,
      completed = false,
      helpUsed = json.helpUsed
    )
  }
}
