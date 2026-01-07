/*
 * Nom     : src/main/scala/sudoku/ui/SudokuGridPane.scala
 * Role    : Conteneur graphique de la grille 9x9 de cellules.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.scene.layout.{GridPane, StackPane}
import scalafx.scene.shape.Rectangle
import scalafx.geometry.{Insets, Pos}
import sudoku.model.SudokuGrid

/**
 * Fonction : SudokuGridPane
 * Role     : Conteneur graphique de la grille 9x9 de cellules.
 */
class SudokuGridPane extends StackPane {
  
  private val cellSize = CellView.Size
  private val gridPadding = 15.0
  private val gridTotalSize = cellSize * 9 + gridPadding * 2
  
  /**
   * Rectangle de fond arrondi pour la grille.
   */
  private val gridBackground = new Rectangle {
    width = gridTotalSize
    height = gridTotalSize
    fill = Theme.CellFixed
    arcWidth = 24
    arcHeight = 24
  }
  
  /**
   * Panneau de grille contenant les cellules.
   */
  private val gridPane = new GridPane {
    hgap = 0
    vgap = 0
    padding = Insets(gridPadding)
    alignment = Pos.Center
  }
  
  /**
   * Tableau des cellules 9x9.
   */
  private val cells: Array[Array[CellView]] = Array.tabulate(9, 9) { (r, c) =>
    val cell = new CellView(r, c)
    gridPane.add(cell, c, r)
    cell
  }
  
  children = Seq(gridBackground, gridPane)
  alignment = Pos.Center
  
  /**************************************************
   * --- METHODES D'ACCES AUX CELLULES ---
   **************************************************/
  
  /**
   * Fonction : getCell
   * Role     : Retourne la cellule a la position donnee.
   * Param    : row - ligne (0-8).
   * Param    : col - colonne (0-8).
   * Retour   : CellView correspondante.
   */
  def getCell(row: Int, col: Int): CellView = cells(row)(col)
  
  /**
   * Fonction : getAllCells
   * Role     : Retourne toutes les cellules de la grille.
   * Retour   : Sequence de toutes les CellView.
   */
  def getAllCells: Seq[CellView] = cells.flatten.toSeq
  
  /**************************************************
   * --- METHODES DE MISE A JOUR ---
   **************************************************/
  
  /**
   * Fonction : updateFromGrid
   * Role     : Met a jour l'affichage depuis une grille de donnees.
   * Param    : grid - grille source.
   * Retour   : Unit.
   */
  def updateFromGrid(grid: SudokuGrid): Unit = {
    for {
      r <- 0 until 9
      c <- 0 until 9
    } {
      val cell = cells(r)(c)
      cell.setValue(grid.getValue(r, c))
      cell.setFixed(grid.isFixed(r, c))
    }
  }
  
  /**
   * Fonction : clearSelection
   * Role     : Efface toutes les selections.
   * Retour   : Unit.
   */
  def clearSelection(): Unit = {
    getAllCells.foreach(_.setSelected(false))
  }
  
  /**
   * Fonction : clearHighlights
   * Role     : Efface tous les surlignages.
   * Retour   : Unit.
   */
  def clearHighlights(): Unit = {
    getAllCells.foreach(_.setHighlighted(false))
  }
  
  /**
   * Fonction : clearErrors
   * Role     : Efface tous les indicateurs d'erreur.
   * Retour   : Unit.
   */
  def clearErrors(): Unit = {
    getAllCells.foreach(_.setError(false))
  }
  
  /**
   * Fonction : clearCorrect
   * Role     : Efface tous les indicateurs de valeur correcte.
   * Retour   : Unit.
   */
  def clearCorrect(): Unit = {
    getAllCells.foreach(_.setCorrect(false))
  }
  
  /**
   * Fonction : setCorrect
   * Role     : Marque une cellule comme correcte.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : isCorrect - true si correcte.
   * Retour   : Unit.
   */
  def setCorrect(row: Int, col: Int, isCorrect: Boolean): Unit = {
    cells(row)(col).setCorrect(isCorrect)
  }
  
  /**
   * Fonction : clearConflicts
   * Role     : Efface tous les indicateurs de conflit.
   * Retour   : Unit.
   */
  def clearConflicts(): Unit = {
    getAllCells.foreach(_.setConflict(false))
  }
  
  /**
   * Fonction : highlightValue
   * Role     : Surligne toutes les cellules contenant une valeur.
   * Param    : value - valeur a surligner.
   * Retour   : Unit.
   */
  def highlightValue(value: Int): Unit = {
    clearHighlights()
    if (value > 0) {
      getAllCells.filter(_.getValue == value).foreach(_.setHighlighted(true))
    }
  }
  
  /**
   * Fonction : showConflicts
   * Role     : Marque les cellules en conflit.
   * Param    : positions - ensemble des positions en conflit.
   * Retour   : Unit.
   */
  def showConflicts(positions: Set[(Int, Int)]): Unit = {
    clearConflicts()
    positions.foreach { case (r, c) => cells(r)(c).setConflict(true) }
  }
  
  /**
   * Fonction : setError
   * Role     : Marque une cellule comme erreur.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Param    : isError - true si erreur.
   * Retour   : Unit.
   */
  def setError(row: Int, col: Int, isError: Boolean): Unit = {
    cells(row)(col).setError(isError)
  }
  
  /**
   * Fonction : clearRelated
   * Role     : Efface tous les surlignages de cases liees.
   * Retour   : Unit.
   */
  def clearRelated(): Unit = {
    getAllCells.foreach(_.setRelated(false))
  }
  
  /**
   * Fonction : showRelatedCells
   * Role     : Surligne les cases liees (meme ligne, colonne, bloc 3x3).
   * Param    : row - ligne de reference.
   * Param    : col - colonne de reference.
   * Retour   : Unit.
   */
  def showRelatedCells(row: Int, col: Int): Unit = {
    clearRelated()
    
    // Surligner toute la ligne
    for (c <- 0 until 9) {
      cells(row)(c).setRelated(true)
    }
    
    // Surligner toute la colonne
    for (r <- 0 until 9) {
      cells(r)(col).setRelated(true)
    }
    
    // Surligner tout le bloc 3x3
    val boxStartRow = (row / 3) * 3
    val boxStartCol = (col / 3) * 3
    for {
      r <- boxStartRow until boxStartRow + 3
      c <- boxStartCol until boxStartCol + 3
    } {
      cells(r)(c).setRelated(true)
    }
  }
}
