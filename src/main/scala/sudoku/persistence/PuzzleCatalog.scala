/*
 * Nom     : src/main/scala/sudoku/persistence/PuzzleCatalog.scala
 * Role    : Gestion du catalogue de puzzles pre-generes.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.persistence

import sudoku.model._
import sudoku.logic.Generator
import upickle.default._
import java.io.{File, PrintWriter}
import java.time.Instant
import java.util.UUID
import scala.io.Source
import scala.util.Using

/**
 * Fonction : PuzzleEntry
 * Role     : Represente une entree du catalogue de puzzles.
 * Param    : id - identifiant unique du puzzle.
 * Param    : difficulty - niveau de difficulte.
 * Param    : grid - grille du puzzle.
 * Param    : solution - grille solution.
 * Param    : createdAt - date de creation.
 */
case class PuzzleEntry(
  id: String,
  difficulty: String,
  grid: Seq[Seq[Int]],
  solution: Seq[Seq[Int]],
  createdAt: String
)

/**
 * Fonction : Catalog
 * Role     : Represente le catalogue complet des puzzles.
 * Param    : puzzles - sequence des entrees du catalogue.
 */
case class Catalog(
  puzzles: Seq[PuzzleEntry]
)

/**
 * Objet de gestion du catalogue de puzzles.
 */
object PuzzleCatalog {
  
  implicit val puzzleEntryRw: ReadWriter[PuzzleEntry] = macroRW
  implicit val catalogRw: ReadWriter[Catalog] = macroRW
  
  val catalogPath: String = "data/catalog.json"
  
  /**
   * Fonction : loadCatalog
   * Role     : Charge le catalogue depuis le fichier JSON.
   * Retour   : Catalog charge ou vide si erreur.
   */
  def loadCatalog(): Catalog = {
    val file = new File(catalogPath)
    if (file.exists()) {
      try {
        val source = Source.fromFile(file)
        val content = try source.mkString finally source.close()
        read[Catalog](content)
      } catch {
        case _: Exception => Catalog(Seq.empty)
      }
    } else {
      Catalog(Seq.empty)
    }
  }
  
  /**
   * Fonction : saveCatalog
   * Role     : Sauvegarde le catalogue dans le fichier JSON.
   * Param    : catalog - catalogue a sauvegarder.
   * Retour   : true si la sauvegarde a reussi.
   */
  def saveCatalog(catalog: Catalog): Boolean = {
    try {
      new File("data").mkdirs()
      val jsonString = write(catalog, indent = 2)
      Using(new PrintWriter(new File(catalogPath))) { writer =>
        writer.write(jsonString)
      }
      true
    } catch {
      case _: Exception => false
    }
  }
  
  /**
   * Fonction : addPuzzle
   * Role     : Ajoute un puzzle au catalogue.
   * Param    : puzzle - grille du puzzle.
   * Param    : solution - grille solution.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : PuzzleEntry cree.
   */
  def addPuzzle(puzzle: SudokuGrid, solution: SudokuGrid, difficulty: Difficulty): PuzzleEntry = {
    val entry = PuzzleEntry(
      id = UUID.randomUUID().toString,
      difficulty = difficulty.name,
      grid = gridToSeq(puzzle),
      solution = gridToSeq(solution),
      createdAt = Instant.now().toString
    )
    
    val catalog = loadCatalog()
    val newCatalog = catalog.copy(puzzles = catalog.puzzles :+ entry)
    saveCatalog(newCatalog)
    
    entry
  }
  
  /**
   * Fonction : generateAndAdd
   * Role     : Genere un nouveau puzzle et l'ajoute au catalogue.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : PuzzleEntry genere.
   */
  def generateAndAdd(difficulty: Difficulty): PuzzleEntry = {
    val (puzzle, solution) = Generator.generate(difficulty)
    addPuzzle(puzzle, solution, difficulty)
  }
  
  /**
   * Fonction : getPuzzle
   * Role     : Recupere un puzzle par son identifiant.
   * Param    : id - identifiant du puzzle.
   * Retour   : Option contenant l'entree trouvee.
   */
  def getPuzzle(id: String): Option[PuzzleEntry] = {
    loadCatalog().puzzles.find(_.id == id)
  }
  
  /**
   * Fonction : removePuzzle
   * Role     : Supprime un puzzle du catalogue.
   * Param    : id - identifiant du puzzle a supprimer.
   * Retour   : true si la suppression a reussi.
   */
  def removePuzzle(id: String): Boolean = {
    val catalog = loadCatalog()
    val newPuzzles = catalog.puzzles.filterNot(_.id == id)
    if (newPuzzles.length != catalog.puzzles.length) {
      saveCatalog(catalog.copy(puzzles = newPuzzles))
      true
    } else {
      false
    }
  }
  
  /**
   * Fonction : createGameState
   * Role     : Cree un GameState a partir d'une entree du catalogue.
   * Param    : entry - entree du catalogue.
   * Retour   : GameState initialise pour jouer.
   */
  def createGameState(entry: PuzzleEntry): GameState = {
    val gridArray = entry.grid.map(_.toArray).toArray
    val solutionArray = entry.solution.map(_.toArray).toArray
    
    val fixedSet = (for {
      r <- 0 until 9
      c <- 0 until 9
      if gridArray(r)(c) != 0
    } yield (r, c)).toSet
    
    val puzzle = SudokuGrid.fromArrayWithFixed(gridArray, fixedSet)
    val solution = SudokuGrid.fromArray(solutionArray, allFixed = false)
    
    GameState.create(
      puzzleId = entry.id,
      puzzle = puzzle,
      solution = solution,
      difficulty = Difficulty.fromString(entry.difficulty)
    )
  }
  
  /**
   * Fonction : getPuzzlesByDifficulty
   * Role     : Liste les puzzles filtres par difficulte.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : Sequence des puzzles correspondants.
   */
  def getPuzzlesByDifficulty(difficulty: Difficulty): Seq[PuzzleEntry] = {
    loadCatalog().puzzles.filter(_.difficulty == difficulty.name)
  }
  
  /**
   * Fonction : initializeWithDefaults
   * Role     : Initialise le catalogue avec des puzzles par defaut si vide.
   * Retour   : Unit.
   */
  def initializeWithDefaults(): Unit = {
    val catalog = loadCatalog()
    if (catalog.puzzles.isEmpty) {
      generateAndAdd(Difficulty.Easy)
      generateAndAdd(Difficulty.Medium)
      generateAndAdd(Difficulty.Hard)
    }
  }
  
  /**
   * Fonction : gridToSeq
   * Role     : Convertit une grille en sequence pour serialisation.
   * Param    : grid - grille a convertir.
   * Retour   : Sequence des valeurs.
   */
  private def gridToSeq(grid: SudokuGrid): Seq[Seq[Int]] = {
    (0 until 9).map { r =>
      (0 until 9).map(c => grid.getValue(r, c))
    }
  }
}
