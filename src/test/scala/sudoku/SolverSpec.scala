/*
 * Nom     : src/test/scala/sudoku/SolverSpec.scala
 * Role    : Tests unitaires pour le solveur de grilles Sudoku.
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
import sudoku.model.SudokuGrid
import sudoku.logic.Solver

/**
 * Fonction : SolverSpec
 * Role     : Tests unitaires pour le solveur de grilles Sudoku.
 */
class SolverSpec extends AnyFlatSpec with Matchers {

  /** Grille facile avec solution unique */
  val easyPuzzle = """
    530070000
    600195000
    098000060
    800060003
    400803001
    700020006
    060000280
    000419005
    000080079
  """.replaceAll("\\s", "")

  /** Solution attendue */
  val easySolution = """
    534678912
    672195348
    198342567
    859761423
    426853791
    713924856
    961537284
    287419635
    345286179
  """.replaceAll("\\s", "")

  "Solver" should "solve a valid puzzle" in {
    val grid = SudokuGrid.fromString(easyPuzzle)
    val solution = Solver.solve(grid)
    
    solution shouldBe defined
    solution.get.isComplete shouldBe true
  }

  it should "find the correct solution" in {
    val grid = SudokuGrid.fromString(easyPuzzle)
    val expected = SudokuGrid.fromString(easySolution)
    val solution = Solver.solve(grid)
    
    solution shouldBe defined
    for {
      r <- 0 until 9
      c <- 0 until 9
    } {
      solution.get.getValue(r, c) shouldBe expected.getValue(r, c)
    }
  }

  it should "detect unique solution" in {
    val grid = SudokuGrid.fromString(easyPuzzle)
    Solver.hasUniqueSolution(grid) shouldBe true
  }

  it should "count multiple solutions for empty grid" in {
    val grid = SudokuGrid.empty
    Solver.countSolutions(grid, 3) shouldBe 3
    Solver.hasUniqueSolution(grid) shouldBe false
  }

  it should "detect unsolvable grid" in {
    val grid = SudokuGrid.empty
      .set(0, 0, 1)
      .set(0, 1, 1)
    
    Solver.isSolvable(grid) shouldBe false
  }

  it should "solve with optimized algorithm" in {
    val grid = SudokuGrid.fromString(easyPuzzle)
    val solution = Solver.solveOptimized(grid)
    
    solution shouldBe defined
    solution.get.isComplete shouldBe true
  }

  it should "solve empty grid" in {
    val grid = SudokuGrid.empty
    val solution = Solver.solve(grid)
    
    solution shouldBe defined
    solution.get.isComplete shouldBe true
  }

  it should "solve hard puzzle in reasonable time" in {
    val hardPuzzle = "800000000003600000070090200050007000000045700000100030001000068008500010090000400"
    val grid = SudokuGrid.fromString(hardPuzzle)
    
    val start = System.currentTimeMillis()
    val solution = Solver.solve(grid)
    val elapsed = System.currentTimeMillis() - start
    
    solution shouldBe defined
    elapsed should be < 5000L
  }
}
