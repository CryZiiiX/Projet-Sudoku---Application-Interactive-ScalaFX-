/*
 * Nom     : src/main/scala/sudoku/model/SudokuGrid.scala
 * Role    : Representation immutable de la grille Sudoku 9x9.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.model

/**
 * Fonction : SudokuGrid
 * Role     : Represente une grille Sudoku 9x9 immutable.
 * Param    : cells - matrice de cellules.
 */
class SudokuGrid private (private val cells: Vector[Vector[Cell]]) {
  
  /**
   * Fonction : apply
   * Role     : Acces direct a une cellule par indices.
   * Param    : row - ligne (0-8).
   * Param    : col - colonne (0-8).
   * Retour   : Cell a la position donnee.
   */
  def apply(row: Int, col: Int): Cell = cells(row)(col)
  
  /**
   * Fonction : get
   * Role     : Recupere une cellule par ses coordonnees.
   * Param    : row - ligne (0-8).
   * Param    : col - colonne (0-8).
   * Retour   : Cell a la position donnee.
   */
  def get(row: Int, col: Int): Cell = cells(row)(col)
  
  /**
   * Fonction : set
   * Role     : Cree une nouvelle grille avec une valeur modifiee.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : value - nouvelle valeur.
   * Retour   : SudokuGrid avec la modification.
   */
  def set(row: Int, col: Int, value: Int): SudokuGrid = {
    val cell = cells(row)(col)
    if (cell.fixed) this
    else {
      val newCell = cell.copy(value = value)
      val newRow = cells(row).updated(col, newCell)
      new SudokuGrid(cells.updated(row, newRow))
    }
  }
  
  /**
   * Fonction : clear
   * Role     : Vide une cellule (met sa valeur a 0).
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : SudokuGrid avec la cellule videe.
   */
  def clear(row: Int, col: Int): SudokuGrid = set(row, col, 0)
  
  /**
   * Fonction : isFixed
   * Role     : Verifie si une cellule est fixe.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : true si la cellule est fixe.
   */
  def isFixed(row: Int, col: Int): Boolean = cells(row)(col).fixed
  
  /**
   * Fonction : getValue
   * Role     : Recupere la valeur d'une cellule.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Valeur de la cellule (0-9).
   */
  def getValue(row: Int, col: Int): Int = cells(row)(col).value
  
  /**
   * Fonction : isEmpty
   * Role     : Verifie si une cellule est vide.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : true si la cellule est vide.
   */
  def isEmpty(row: Int, col: Int): Boolean = cells(row)(col).isEmpty
  
  /**
   * Fonction : row
   * Role     : Retourne toutes les valeurs d'une ligne.
   * Param    : r - indice de la ligne (0-8).
   * Retour   : Sequence des valeurs de la ligne.
   */
  def row(r: Int): Seq[Int] = cells(r).map(_.value)
  
  /**
   * Fonction : col
   * Role     : Retourne toutes les valeurs d'une colonne.
   * Param    : c - indice de la colonne (0-8).
   * Retour   : Sequence des valeurs de la colonne.
   */
  def col(c: Int): Seq[Int] = cells.map(_(c).value)
  
  /**
   * Fonction : box
   * Role     : Retourne toutes les valeurs d'un bloc 3x3.
   * Param    : boxRow - indice du bloc en ligne (0-2).
   * Param    : boxCol - indice du bloc en colonne (0-2).
   * Retour   : Sequence des valeurs du bloc.
   */
  def box(boxRow: Int, boxCol: Int): Seq[Int] = {
    val startRow = boxRow * 3
    val startCol = boxCol * 3
    for {
      r <- startRow until startRow + 3
      c <- startCol until startCol + 3
    } yield cells(r)(c).value
  }
  
  /**
   * Fonction : boxContaining
   * Role     : Retourne le bloc 3x3 contenant une cellule.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Sequence des valeurs du bloc.
   */
  def boxContaining(row: Int, col: Int): Seq[Int] = box(row / 3, col / 3)
  
  /**
   * Fonction : isComplete
   * Role     : Verifie si toutes les cellules sont remplies.
   * Retour   : true si aucune cellule n'est vide.
   */
  def isComplete: Boolean = cells.forall(_.forall(_.isSet))
  
  /**
   * Fonction : findEmptyCell
   * Role     : Trouve la premiere cellule vide pour le solveur.
   * Retour   : Option contenant les coordonnees de la cellule vide.
   */
  def findEmptyCell(): Option[(Int, Int)] = {
    for {
      r <- 0 until 9
      c <- 0 until 9
      if cells(r)(c).isEmpty
    } return Some((r, c))
    None
  }
  
  /**
   * Fonction : fixedPositions
   * Role     : Retourne l'ensemble des positions des cellules fixes.
   * Retour   : Set des coordonnees des cellules fixes.
   */
  def fixedPositions: Set[(Int, Int)] = {
    (for {
      r <- 0 until 9
      c <- 0 until 9
      if cells(r)(c).fixed
    } yield (r, c)).toSet
  }
  
  /**
   * Fonction : copy
   * Role     : Cree une copie profonde de la grille.
   * Retour   : Nouvelle SudokuGrid identique.
   */
  def copy(): SudokuGrid = new SudokuGrid(cells)
  
  /**
   * Fonction : toArray
   * Role     : Convertit la grille en tableau 2D pour serialisation.
   * Retour   : Tableau 2D des valeurs.
   */
  def toArray: Array[Array[Int]] = cells.map(_.map(_.value).toArray).toArray
  
  /**
   * Fonction : toString
   * Role     : Represente la grille sous forme textuelle formatee.
   * Retour   : Chaine formatee de la grille.
   */
  override def toString: String = {
    val sb = new StringBuilder
    for (r <- 0 until 9) {
      if (r % 3 == 0 && r != 0) sb.append("------+-------+------\n")
      for (c <- 0 until 9) {
        if (c % 3 == 0 && c != 0) sb.append("| ")
        val v = cells(r)(c).value
        sb.append(if (v == 0) ". " else s"$v ")
      }
      sb.append("\n")
    }
    sb.toString()
  }
}

/**
 * Objet compagnon pour la creation de grilles Sudoku.
 */
object SudokuGrid {
  
  /**
   * Fonction : empty
   * Role     : Cree une grille vide de 9x9.
   * Retour   : SudokuGrid vide.
   */
  def empty: SudokuGrid = {
    val cells = Vector.fill(9, 9)(Cell.empty)
    new SudokuGrid(cells)
  }
  
  /**
   * Fonction : fromArray
   * Role     : Cree une grille a partir d'un tableau 2D.
   * Param    : arr - tableau 2D des valeurs.
   * Param    : allFixed - si true, marque les valeurs non nulles comme fixes.
   * Retour   : SudokuGrid initialisee.
   */
  def fromArray(arr: Array[Array[Int]], allFixed: Boolean = true): SudokuGrid = {
    val cells = Vector.tabulate(9, 9) { (r, c) =>
      val v = arr(r)(c)
      if (v == 0) Cell.empty
      else if (allFixed) Cell.fixed(v)
      else Cell.editable(v)
    }
    new SudokuGrid(cells)
  }
  
  /**
   * Fonction : fromArrayWithFixed
   * Role     : Cree une grille avec des positions fixes specifiees.
   * Param    : arr - tableau 2D des valeurs.
   * Param    : fixed - ensemble des positions fixes.
   * Retour   : SudokuGrid avec cellules fixes definies.
   */
  def fromArrayWithFixed(arr: Array[Array[Int]], fixed: Set[(Int, Int)]): SudokuGrid = {
    val cells = Vector.tabulate(9, 9) { (r, c) =>
      val v = arr(r)(c)
      if (fixed.contains((r, c))) Cell.fixed(v)
      else if (v == 0) Cell.empty
      else Cell.editable(v)
    }
    new SudokuGrid(cells)
  }
  
  /**
   * Fonction : fromString
   * Role     : Cree une grille a partir d'une chaine de 81 caracteres.
   * Param    : s - chaine de caracteres (0 ou . pour vide).
   * Retour   : SudokuGrid initialisee.
   */
  def fromString(s: String): SudokuGrid = {
    val cleaned = s.filter(c => c.isDigit || c == '.')
    require(cleaned.length == 81, s"La chaine doit contenir 81 caracteres, trouve: ${cleaned.length}")
    val arr = cleaned.grouped(9).map(_.map(c => if (c == '.') 0 else c.asDigit).toArray).toArray
    fromArray(arr)
  }
}
