/*
 * Nom     : src/main/scala/sudoku/logic/Validator.scala
 * Role    : Validation des regles du Sudoku et detection des conflits.
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
 * Trait representant une violation des regles du Sudoku.
 */
sealed trait Violation {
  def row: Int
  def col: Int
  def conflictRow: Int
  def conflictCol: Int
}

/**
 * Fonction : RowViolation
 * Role     : Represente un conflit dans une ligne.
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : conflictCol - colonne de la cellule en conflit.
 */
case class RowViolation(row: Int, col: Int, conflictCol: Int) extends Violation {
  def conflictRow: Int = row
}

/**
 * Fonction : ColViolation
 * Role     : Represente un conflit dans une colonne.
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : conflictRow - ligne de la cellule en conflit.
 */
case class ColViolation(row: Int, col: Int, conflictRow: Int) extends Violation {
  def conflictCol: Int = col
}

/**
 * Fonction : BoxViolation
 * Role     : Represente un conflit dans un bloc 3x3.
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : conflictRow - ligne de la cellule en conflit.
 * Param    : conflictCol - colonne de la cellule en conflit.
 */
case class BoxViolation(row: Int, col: Int, conflictRow: Int, conflictCol: Int) extends Violation

/**
 * Objet de validation des regles Sudoku.
 */
object Validator {
  
  /**
   * Fonction : isMoveValid
   * Role     : Verifie si placer une valeur a une position est valide.
   * Param    : grid - grille actuelle.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : value - valeur a placer.
   * Retour   : true si aucune violation n'est detectee.
   */
  def isMoveValid(grid: SudokuGrid, row: Int, col: Int, value: Int): Boolean = {
    if (value == 0) return true
    getViolations(grid, row, col, value).isEmpty
  }
  
  /**
   * Fonction : getViolations
   * Role     : Retourne toutes les violations pour un placement donne.
   * Param    : grid - grille actuelle.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : value - valeur a placer.
   * Retour   : Set des violations detectees.
   */
  def getViolations(grid: SudokuGrid, row: Int, col: Int, value: Int): Set[Violation] = {
    if (value == 0) return Set.empty
    
    var violations = Set.empty[Violation]
    
    // Verifier la ligne
    for (c <- 0 until 9 if c != col) {
      if (grid.getValue(row, c) == value) {
        violations += RowViolation(row, col, c)
      }
    }
    
    // Verifier la colonne
    for (r <- 0 until 9 if r != row) {
      if (grid.getValue(r, col) == value) {
        violations += ColViolation(row, col, r)
      }
    }
    
    // Verifier le bloc 3x3
    val boxStartRow = (row / 3) * 3
    val boxStartCol = (col / 3) * 3
    for {
      r <- boxStartRow until boxStartRow + 3
      c <- boxStartCol until boxStartCol + 3
      if r != row || c != col
    } {
      if (grid.getValue(r, c) == value) {
        violations += BoxViolation(row, col, r, c)
      }
    }
    
    violations
  }
  
  /**
   * Fonction : isGridValid
   * Role     : Verifie si la grille actuelle est valide globalement.
   * Param    : grid - grille a verifier.
   * Retour   : true si aucune violation n'existe.
   */
  def isGridValid(grid: SudokuGrid): Boolean = {
    for {
      r <- 0 until 9
      c <- 0 until 9
      value = grid.getValue(r, c)
      if value != 0
    } {
      val tempGrid = grid.set(r, c, 0)
      if (!isMoveValid(tempGrid, r, c, value)) {
        return false
      }
    }
    true
  }
  
  /**
   * Fonction : getAllConflicts
   * Role     : Retourne toutes les cellules en conflit dans la grille.
   * Param    : grid - grille a analyser.
   * Retour   : Map des positions vers leurs violations.
   */
  def getAllConflicts(grid: SudokuGrid): Map[(Int, Int), Set[Violation]] = {
    var conflicts = Map.empty[(Int, Int), Set[Violation]]
    
    for {
      r <- 0 until 9
      c <- 0 until 9
      value = grid.getValue(r, c)
      if value != 0
    } {
      val tempGrid = grid.set(r, c, 0)
      val violations = getViolations(tempGrid, r, c, value)
      if (violations.nonEmpty) {
        conflicts += (r, c) -> violations
      }
    }
    
    conflicts
  }
  
  /**
   * Fonction : getConflictingCells
   * Role     : Retourne les positions en conflit avec une cellule donnee.
   * Param    : grid - grille actuelle.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Set des positions en conflit.
   */
  def getConflictingCells(grid: SudokuGrid, row: Int, col: Int): Set[(Int, Int)] = {
    val value = grid.getValue(row, col)
    if (value == 0) return Set.empty
    
    val tempGrid = grid.set(row, col, 0)
    val violations = getViolations(tempGrid, row, col, value)
    
    violations.map(v => (v.conflictRow, v.conflictCol))
  }
  
  /**
   * Fonction : getRowCells
   * Role     : Retourne toutes les cellules d'une ligne.
   * Param    : row - indice de la ligne.
   * Retour   : Set des positions de la ligne.
   */
  def getRowCells(row: Int): Set[(Int, Int)] = {
    (0 until 9).map(c => (row, c)).toSet
  }
  
  /**
   * Fonction : getColCells
   * Role     : Retourne toutes les cellules d'une colonne.
   * Param    : col - indice de la colonne.
   * Retour   : Set des positions de la colonne.
   */
  def getColCells(col: Int): Set[(Int, Int)] = {
    (0 until 9).map(r => (r, col)).toSet
  }
  
  /**
   * Fonction : getBoxCells
   * Role     : Retourne toutes les cellules d'un bloc 3x3.
   * Param    : row - ligne de reference.
   * Param    : col - colonne de reference.
   * Retour   : Set des positions du bloc.
   */
  def getBoxCells(row: Int, col: Int): Set[(Int, Int)] = {
    val boxStartRow = (row / 3) * 3
    val boxStartCol = (col / 3) * 3
    (for {
      r <- boxStartRow until boxStartRow + 3
      c <- boxStartCol until boxStartCol + 3
    } yield (r, c)).toSet
  }
  
  /**
   * Fonction : getRelatedCells
   * Role     : Retourne toutes les cellules liees (ligne, colonne, bloc).
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Set des positions liees (sans la cellule elle-meme).
   */
  def getRelatedCells(row: Int, col: Int): Set[(Int, Int)] = {
    getRowCells(row) ++ getColCells(col) ++ getBoxCells(row, col) - ((row, col))
  }
  
  /**
   * Fonction : findCellsWithValue
   * Role     : Trouve toutes les cellules contenant une valeur donnee.
   * Param    : grid - grille a analyser.
   * Param    : value - valeur recherchee.
   * Retour   : Set des positions contenant la valeur.
   */
  def findCellsWithValue(grid: SudokuGrid, value: Int): Set[(Int, Int)] = {
    (for {
      r <- 0 until 9
      c <- 0 until 9
      if grid.getValue(r, c) == value
    } yield (r, c)).toSet
  }
}
