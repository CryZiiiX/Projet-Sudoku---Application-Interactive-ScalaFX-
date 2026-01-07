/*
 * Nom     : src/test/scala/sudoku/PersistenceSpec.scala
 * Role    : Tests unitaires pour la sauvegarde et le chargement des parties.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli test src/
 */
package sudoku

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sudoku.model._
import sudoku.logic.Generator
import sudoku.persistence._
import java.io.File

/**
 * Fonction : PersistenceSpec
 * Role     : Tests unitaires pour la sauvegarde et le chargement des parties.
 */
class PersistenceSpec extends AnyFlatSpec with Matchers {

  "GameRepository" should "save and load game state" in {
    val (puzzle, solution) = Generator.generate(Difficulty.Easy)
    val state = GameState.create("test-puzzle", puzzle, solution, Difficulty.Easy)
    
    val stateWithMoves = state
      .setValue(0, 2, 5)
      .setValue(1, 0, 1)
    
    val testPath = "test_save.json"
    
    try {
      GameRepository.save(stateWithMoves, testPath) shouldBe true
      
      val loaded = GameRepository.load(testPath)
      loaded shouldBe defined
      
      loaded.get.id shouldBe stateWithMoves.id
      loaded.get.puzzleId shouldBe stateWithMoves.puzzleId
      loaded.get.errorCount shouldBe stateWithMoves.errorCount
      
      for {
        r <- 0 until 9
        c <- 0 until 9
      } {
        loaded.get.grid.getValue(r, c) shouldBe stateWithMoves.grid.getValue(r, c)
        loaded.get.grid.isFixed(r, c) shouldBe stateWithMoves.grid.isFixed(r, c)
      }
    } finally {
      new File(testPath).delete()
    }
  }

  "PuzzleCatalog" should "add and retrieve puzzles" in {
    val (puzzle, solution) = Generator.generate(Difficulty.Medium)
    
    val entry = PuzzleCatalog.addPuzzle(puzzle, solution, Difficulty.Medium)
    
    entry.id should not be empty
    entry.difficulty shouldBe "Moyen"
    
    val retrieved = PuzzleCatalog.getPuzzle(entry.id)
    retrieved shouldBe defined
    retrieved.get.id shouldBe entry.id
    
    PuzzleCatalog.removePuzzle(entry.id)
  }

  it should "create game state from entry" in {
    val (puzzle, solution) = Generator.generate(Difficulty.Hard)
    val entry = PuzzleCatalog.addPuzzle(puzzle, solution, Difficulty.Hard)
    
    val state = PuzzleCatalog.createGameState(entry)
    
    state.puzzleId shouldBe entry.id
    state.difficulty shouldBe Difficulty.Hard
    state.errorCount shouldBe 0
    
    for {
      r <- 0 until 9
      c <- 0 until 9
    } {
      state.grid.getValue(r, c) shouldBe puzzle.getValue(r, c)
    }
    
    PuzzleCatalog.removePuzzle(entry.id)
  }
}
