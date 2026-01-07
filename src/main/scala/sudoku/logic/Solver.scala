/*
 * Nom     : src/main/scala/sudoku/logic/Solver.scala
 * Role    : Resolution de grilles Sudoku par algorithme de backtracking.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.logic

import sudoku.model.SudokuGrid

/**
 * Objet de resolution de grilles Sudoku par backtracking.
 */
object Solver {
  
  /**
   * Fonction : solve
   * Role     : Resout la grille et retourne la solution si elle existe.
   * Param    : grid - grille a resoudre.
   * Retour   : Option contenant la grille resolue.
   */
  def solve(grid: SudokuGrid): Option[SudokuGrid] = {
    solveInternal(grid.toArray) match {
      case Some(arr) => Some(SudokuGrid.fromArray(arr, allFixed = false))
      case None => None
    }
  }
  
  /**
   * Fonction : countSolutions
   * Role     : Compte le nombre de solutions jusqu'a un maximum.
   * Param    : grid - grille a analyser.
   * Param    : maxSolutions - nombre maximum de solutions a compter.
   * Retour   : Nombre de solutions trouvees.
   */
  def countSolutions(grid: SudokuGrid, maxSolutions: Int = 2): Int = {
    val arr = grid.toArray
    countSolutionsInternal(arr, maxSolutions)
  }
  
  /**
   * Fonction : hasUniqueSolution
   * Role     : Verifie si la grille a exactement une solution.
   * Param    : grid - grille a verifier.
   * Retour   : true si la solution est unique.
   */
  def hasUniqueSolution(grid: SudokuGrid): Boolean = {
    countSolutions(grid, 2) == 1
  }
  
  /**
   * Fonction : isSolvable
   * Role     : Verifie si la grille a au moins une solution.
   * Param    : grid - grille a verifier.
   * Retour   : true si la grille est soluble.
   */
  def isSolvable(grid: SudokuGrid): Boolean = {
    countSolutions(grid, 1) >= 1
  }
  
  /**
   * Fonction : solveInternal
   * Role     : Implementation interne du solveur avec tableau mutable.
   * Param    : grid - tableau 2D mutable.
   * Retour   : Option contenant le tableau resolu.
   */
  private def solveInternal(grid: Array[Array[Int]]): Option[Array[Array[Int]]] = {
    findEmptyCell(grid) match {
      case None => 
        Some(grid.map(_.clone()))
      case Some((row, col)) =>
        for (num <- 1 to 9) {
          if (isValid(grid, row, col, num)) {
            grid(row)(col) = num
            solveInternal(grid) match {
              case Some(solution) => return Some(solution)
              case None =>
            }
            grid(row)(col) = 0
          }
        }
        None
    }
  }
  
  /**
   * Fonction : countSolutionsInternal
   * Role     : Compte les solutions avec arret anticipe.
   * Param    : grid - tableau 2D mutable.
   * Param    : maxSolutions - limite de solutions a compter.
   * Retour   : Nombre de solutions trouvees.
   */
  private def countSolutionsInternal(grid: Array[Array[Int]], maxSolutions: Int): Int = {
    findEmptyCell(grid) match {
      case None =>
        1
      case Some((row, col)) =>
        var count = 0
        for (num <- 1 to 9 if count < maxSolutions) {
          if (isValid(grid, row, col, num)) {
            grid(row)(col) = num
            count += countSolutionsInternal(grid, maxSolutions - count)
            grid(row)(col) = 0
          }
        }
        count
    }
  }
  
  /**
   * Fonction : findEmptyCell
   * Role     : Trouve la premiere cellule vide dans le tableau.
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
    // Verifier la ligne
    for (c <- 0 until 9) {
      if (grid(row)(c) == num) return false
    }
    
    // Verifier la colonne
    for (r <- 0 until 9) {
      if (grid(r)(col) == num) return false
    }
    
    // Verifier le bloc 3x3
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
   * Fonction : solveOptimized
   * Role     : Resout avec l'heuristique MRV (Minimum Remaining Values).
   * Param    : grid - grille a resoudre.
   * Retour   : Option contenant la grille resolue.
   */
  def solveOptimized(grid: SudokuGrid): Option[SudokuGrid] = {
    val arr = grid.toArray
    solveOptimizedInternal(arr) match {
      case true => Some(SudokuGrid.fromArray(arr, allFixed = false))
      case false => None
    }
  }
  
  /**
   * Fonction : solveOptimizedInternal
   * Role     : Implementation interne du solveur optimise.
   * Param    : grid - tableau 2D mutable.
   * Retour   : true si une solution a ete trouvee.
   */
  private def solveOptimizedInternal(grid: Array[Array[Int]]): Boolean = {
    findBestEmptyCell(grid) match {
      case None => true
      case Some((row, col, candidates)) =>
        for (num <- candidates) {
          grid(row)(col) = num
          if (solveOptimizedInternal(grid)) return true
          grid(row)(col) = 0
        }
        false
    }
  }
  
  /**
   * Fonction : findBestEmptyCell
   * Role     : Trouve la cellule vide avec le moins de candidats.
   * Param    : grid - tableau 2D.
   * Retour   : Option contenant coordonnees et candidats.
   */
  private def findBestEmptyCell(grid: Array[Array[Int]]): Option[(Int, Int, Seq[Int])] = {
    var best: Option[(Int, Int, Seq[Int])] = None
    var minCandidates = 10
    
    for {
      r <- 0 until 9
      c <- 0 until 9
      if grid(r)(c) == 0
    } {
      val candidates = getCandidates(grid, r, c)
      if (candidates.isEmpty) return Some((r, c, Seq.empty))
      if (candidates.length < minCandidates) {
        minCandidates = candidates.length
        best = Some((r, c, candidates))
      }
    }
    best
  }
  
  /**
   * Fonction : getCandidates
   * Role     : Retourne les valeurs possibles pour une cellule.
   * Param    : grid - tableau 2D.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Sequence des valeurs candidates.
   */
  private def getCandidates(grid: Array[Array[Int]], row: Int, col: Int): Seq[Int] = {
    val used = scala.collection.mutable.Set[Int]()
    
    // Valeurs dans la ligne
    for (c <- 0 until 9) used += grid(row)(c)
    
    // Valeurs dans la colonne
    for (r <- 0 until 9) used += grid(r)(col)
    
    // Valeurs dans le bloc
    val boxRow = (row / 3) * 3
    val boxCol = (col / 3) * 3
    for {
      r <- boxRow until boxRow + 3
      c <- boxCol until boxCol + 3
    } used += grid(r)(c)
    
    (1 to 9).filterNot(used.contains)
  }
}
