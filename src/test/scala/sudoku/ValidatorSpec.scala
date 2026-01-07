/*
 * Nom     : src/test/scala/sudoku/ValidatorSpec.scala
 * Role    : Tests unitaires pour le validateur de regles Sudoku.
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
import sudoku.logic._

/**
 * Fonction : ValidatorSpec
 * Role     : Tests unitaires pour le validateur de regles Sudoku.
 */
class ValidatorSpec extends AnyFlatSpec with Matchers {

  /** Grille valide pour les tests */
  val validGridString = """
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

  "Validator" should "accept valid moves" in {
    val grid = SudokuGrid.fromString(validGridString)
    Validator.isMoveValid(grid, 0, 3, 6) shouldBe true
  }

  it should "detect row violations" in {
    val grid = SudokuGrid.fromString(validGridString)
    val violations = Validator.getViolations(grid, 0, 2, 5)
    violations should not be empty
    violations.exists(_.isInstanceOf[RowViolation]) shouldBe true
  }

  it should "detect column violations" in {
    val grid = SudokuGrid.fromString(validGridString)
    val violations = Validator.getViolations(grid, 2, 0, 6)
    violations should not be empty
    violations.exists(_.isInstanceOf[ColViolation]) shouldBe true
  }

  it should "detect box violations" in {
    val grid = SudokuGrid.fromString(validGridString)
    val violations = Validator.getViolations(grid, 0, 2, 5)
    violations should not be empty
  }

  it should "validate empty grid" in {
    val grid = SudokuGrid.empty
    Validator.isGridValid(grid) shouldBe true
  }

  it should "find cells with specific value" in {
    val grid = SudokuGrid.fromString(validGridString)
    val fives = Validator.findCellsWithValue(grid, 5)
    fives should contain((0, 0))
    fives.size should be >= 1
  }

  it should "get related cells correctly" in {
    val related = Validator.getRelatedCells(4, 4)
    related should contain((4, 0))
    related should contain((4, 8))
    related should contain((0, 4))
    related should contain((8, 4))
    related should contain((3, 3))
    related should contain((5, 5))
    related should not contain((4, 4))
  }

  it should "accept value 0 (empty)" in {
    val grid = SudokuGrid.fromString(validGridString)
    Validator.isMoveValid(grid, 0, 0, 0) shouldBe true
    Validator.getViolations(grid, 0, 0, 0) shouldBe empty
  }

  it should "detect all conflicts in invalid grid" in {
    val invalidGrid = SudokuGrid.empty
      .set(0, 0, 5)
      .set(0, 5, 5)
    
    val conflicts = Validator.getAllConflicts(invalidGrid)
    conflicts should not be empty
    conflicts should contain key (0, 0)
    conflicts should contain key (0, 5)
  }
}
