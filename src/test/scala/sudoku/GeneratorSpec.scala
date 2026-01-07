/*
 * Nom     : src/test/scala/sudoku/GeneratorSpec.scala
 * Role    : Tests unitaires pour le generateur de puzzles Sudoku.
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
import sudoku.model.{SudokuGrid, Difficulty}
import sudoku.logic.{Generator, Solver, Validator}

/**
 * Fonction : GeneratorSpec
 * Role     : Tests unitaires pour le generateur de puzzles Sudoku.
 */
class GeneratorSpec extends AnyFlatSpec with Matchers {

  "Generator" should "generate a complete valid solution" in {
    val solution = Generator.generateCompleteSolution()
    
    solution.isComplete shouldBe true
    Validator.isGridValid(solution) shouldBe true
  }

  it should "generate puzzle with unique solution" in {
    val (puzzle, solution) = Generator.generate(Difficulty.Easy)
    
    puzzle.isComplete shouldBe false
    solution.isComplete shouldBe true
    Solver.hasUniqueSolution(puzzle) shouldBe true
  }

  it should "generate puzzle solvable to the provided solution" in {
    val (puzzle, solution) = Generator.generate(Difficulty.Medium)
    
    val solvedPuzzle = Solver.solve(puzzle)
    solvedPuzzle shouldBe defined
    
    for {
      r <- 0 until 9
      c <- 0 until 9
    } {
      solvedPuzzle.get.getValue(r, c) shouldBe solution.getValue(r, c)
    }
  }

  it should "mark fixed cells correctly" in {
    val (puzzle, _) = Generator.generate(Difficulty.Easy)
    
    for {
      r <- 0 until 9
      c <- 0 until 9
    } {
      if (puzzle.getValue(r, c) != 0) {
        puzzle.isFixed(r, c) shouldBe true
      }
    }
  }

  it should "generate puzzles with appropriate difficulty" in {
    val (easyPuzzle, _) = Generator.generate(Difficulty.Easy)
    val (hardPuzzle, _) = Generator.generate(Difficulty.Hard)
    
    val easyEmpty = countEmptyCells(easyPuzzle)
    val hardEmpty = countEmptyCells(hardPuzzle)
    
    easyEmpty should be >= 30
    easyEmpty should be <= 45
    hardEmpty should be >= 50
  }

  it should "generate different puzzles each time" in {
    val puzzles = (1 to 3).map(_ => Generator.generate(Difficulty.Medium)._1)
    
    val distinctCount = puzzles.map(_.toString).distinct.length
    distinctCount should be >= 2
  }

  /**
   * Fonction : countEmptyCells
   * Role     : Compte le nombre de cellules vides dans une grille.
   * Param    : grid - grille a analyser.
   * Retour   : Nombre de cellules vides.
   */
  private def countEmptyCells(grid: SudokuGrid): Int = {
    (for {
      r <- 0 until 9
      c <- 0 until 9
      if grid.getValue(r, c) == 0
    } yield 1).sum
  }
}
