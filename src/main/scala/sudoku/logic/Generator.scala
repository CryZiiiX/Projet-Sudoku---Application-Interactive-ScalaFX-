/*
 * Nom     : src/main/scala/sudoku/logic/Generator.scala
 * Role    : Generation de puzzles Sudoku avec solution unique.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.logic

import sudoku.model.{SudokuGrid, Difficulty}
import scala.util.Random

/**
 * Objet de generation de puzzles Sudoku avec solution unique.
 */
object Generator {
  
  private val random = new Random()
  
  /**
   * Fonction : generate
   * Role     : Genere un puzzle avec sa solution selon la difficulte.
   * Param    : difficulty - niveau de difficulte souhaite.
   * Retour   : Tuple (puzzle, solution).
   */
  def generate(difficulty: Difficulty): (SudokuGrid, SudokuGrid) = {
    val solution = generateCompleteSolution()
    val puzzle = createPuzzle(solution, difficulty)
    (puzzle, solution)
  }
  
  /**
   * Fonction : generateCompleteSolution
   * Role     : Genere une grille complete valide aleatoirement.
   * Retour   : SudokuGrid complete.
   */
  def generateCompleteSolution(): SudokuGrid = {
    val grid = Array.fill(9, 9)(0)
    fillDiagonalBlocks(grid)
    solveGrid(grid)
    SudokuGrid.fromArray(grid, allFixed = false)
  }
  
  /**
   * Fonction : fillDiagonalBlocks
   * Role     : Remplit les 3 blocs diagonaux independants.
   * Param    : grid - tableau 2D a remplir.
   * Retour   : Unit.
   */
  private def fillDiagonalBlocks(grid: Array[Array[Int]]): Unit = {
    for (block <- 0 until 3) {
      fillBlock(grid, block * 3, block * 3)
    }
  }
  
  /**
   * Fonction : fillBlock
   * Role     : Remplit un bloc 3x3 avec des valeurs aleatoires.
   * Param    : grid - tableau 2D.
   * Param    : startRow - ligne de debut du bloc.
   * Param    : startCol - colonne de debut du bloc.
   * Retour   : Unit.
   */
  private def fillBlock(grid: Array[Array[Int]], startRow: Int, startCol: Int): Unit = {
    val values = random.shuffle((1 to 9).toList)
    var idx = 0
    for {
      r <- startRow until startRow + 3
      c <- startCol until startCol + 3
    } {
      grid(r)(c) = values(idx)
      idx += 1
    }
  }
  
  /**
   * Fonction : solveGrid
   * Role     : Complete la grille par backtracking aleatoire.
   * Param    : grid - tableau 2D partiellement rempli.
   * Retour   : true si la grille a ete completee.
   */
  private def solveGrid(grid: Array[Array[Int]]): Boolean = {
    findEmptyCell(grid) match {
      case None => true
      case Some((row, col)) =>
        val candidates = random.shuffle((1 to 9).toList)
        for (num <- candidates) {
          if (isValid(grid, row, col, num)) {
            grid(row)(col) = num
            if (solveGrid(grid)) return true
            grid(row)(col) = 0
          }
        }
        false
    }
  }
  
  /**
   * Fonction : createPuzzle
   * Role     : Cree le puzzle en retirant des cases tout en gardant l'unicite.
   * Param    : solution - grille solution complete.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : SudokuGrid du puzzle.
   */
  private def createPuzzle(solution: SudokuGrid, difficulty: Difficulty): SudokuGrid = {
    val grid = solution.toArray
    val (minEmpty, maxEmpty) = difficulty.emptyCells
    val targetEmpty = minEmpty + random.nextInt(maxEmpty - minEmpty + 1)
    
    val positions = random.shuffle(
      (for {
        r <- 0 until 9
        c <- 0 until 9
      } yield (r, c)).toList
    )
    
    var emptied = 0
    var attempts = 0
    val maxAttempts = 81 * 2
    
    for ((r, c) <- positions if emptied < targetEmpty && attempts < maxAttempts) {
      val backup = grid(r)(c)
      grid(r)(c) = 0
      
      if (countSolutions(grid, 2) == 1) {
        emptied += 1
      } else {
        grid(r)(c) = backup
      }
      attempts += 1
    }
    
    val fixed = (for {
      r <- 0 until 9
      c <- 0 until 9
      if grid(r)(c) != 0
    } yield (r, c)).toSet
    
    SudokuGrid.fromArrayWithFixed(grid, fixed)
  }
  
  /**
   * Fonction : countSolutions
   * Role     : Compte les solutions avec arret a un maximum.
   * Param    : grid - tableau 2D.
   * Param    : max - nombre maximum de solutions a compter.
   * Retour   : Nombre de solutions trouvees.
   */
  private def countSolutions(grid: Array[Array[Int]], max: Int): Int = {
    findEmptyCell(grid) match {
      case None => 1
      case Some((row, col)) =>
        var count = 0
        for (num <- 1 to 9 if count < max) {
          if (isValid(grid, row, col, num)) {
            grid(row)(col) = num
            count += countSolutions(grid, max - count)
            grid(row)(col) = 0
          }
        }
        count
    }
  }
  
  /**
   * Fonction : findEmptyCell
   * Role     : Trouve la premiere cellule vide.
   * Param    : grid - tableau 2D.
   * Retour   : Option contenant les coordonnees.
   */
  private def findEmptyCell(grid: Array[Array[Int]]): Option[(Int, Int)] = {
    for {
      r <- 0 until 9
      c <- 0 until 9
      if grid(r)(c) == 0
    } return Some((r, c))
    None
  }
  
  /**
   * Fonction : isValid
   * Role     : Verifie si placer un nombre a une position est valide.
   * Param    : grid - tableau 2D.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : num - nombre a placer.
   * Retour   : true si le placement est valide.
   */
  private def isValid(grid: Array[Array[Int]], row: Int, col: Int, num: Int): Boolean = {
    // Verifier ligne
    for (c <- 0 until 9) {
      if (grid(row)(c) == num) return false
    }
    // Verifier colonne
    for (r <- 0 until 9) {
      if (grid(r)(col) == num) return false
    }
    // Verifier bloc
    val boxRow = (row / 3) * 3
    val boxCol = (col / 3) * 3
    for {
      r <- boxRow until boxRow + 3
      c <- boxCol until boxCol + 3
    } {
      if (grid(r)(c) == num) return false
    }
    true
  }
  
  /**
   * Fonction : generateBatch
   * Role     : Genere plusieurs puzzles d'un coup pour le catalogue.
   * Param    : count - nombre de puzzles a generer.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : Liste de tuples (puzzle, solution).
   */
  def generateBatch(count: Int, difficulty: Difficulty): List[(SudokuGrid, SudokuGrid)] = {
    (1 to count).map(_ => generate(difficulty)).toList
  }
}
