/*
 * Nom     : src/main/scala/sudoku/ui/CellView.scala
 * Role    : Composant graphique representant une cellule de la grille.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.scene.layout.StackPane
import scalafx.scene.control.Label
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Font, FontWeight}
import scalafx.geometry.Pos
import scalafx.beans.property.{BooleanProperty, IntegerProperty}

/**
 * Fonction : CellView
 * Role     : Vue graphique d'une cellule du Sudoku avec gestion des etats visuels.
 * Param    : row - ligne de la cellule (0-8).
 * Param    : col - colonne de la cellule (0-8).
 */
class CellView(val row: Int, val col: Int) extends StackPane {
  
  /**************************************************
   * --- PROPRIETES OBSERVABLES ---
   **************************************************/
  
  /** Valeur de la cellule (0-9) */
  val valueProperty: IntegerProperty = IntegerProperty(0)
  
  /** True si la cellule est fixe */
  val fixedProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la cellule est selectionnee */
  val selectedProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la cellule contient une erreur */
  val errorProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la valeur est correcte */
  val correctProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la cellule est surlignee */
  val highlightedProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la cellule est en conflit */
  val conflictProperty: BooleanProperty = BooleanProperty(false)
  
  /** True si la cellule est liee a la selection */
  val relatedProperty: BooleanProperty = BooleanProperty(false)
  
  /**************************************************
   * --- COMPOSANTS GRAPHIQUES ---
   **************************************************/
  
  /** Rectangle de fond de la cellule */
  private val bgRect = new Rectangle {
    width = CellView.Size
    height = CellView.Size
    fill = Theme.CellBackground
    stroke = Theme.BorderLight
    strokeWidth = 0.5
    arcWidth = 4
    arcHeight = 4
  }
  
  /** Label affichant la valeur */
  private val valueLabel = new Label {
    font = Font.font("Georgia", FontWeight.Bold, 36)
    textFill = Theme.TextFixed
    alignment = Pos.Center
  }
  
  children = Seq(bgRect, valueLabel)
  prefWidth = CellView.Size
  prefHeight = CellView.Size
  alignment = Pos.Center
  
  applyBlockBorders()
  
  /**************************************************
   * --- BINDINGS ET LISTENERS ---
   **************************************************/
  
  valueProperty.onChange { (_, _, newVal) =>
    valueLabel.text = if (newVal.intValue() == 0) "" else newVal.toString
  }
  
  fixedProperty.onChange { (_, _, _) => updateStyle() }
  selectedProperty.onChange { (_, _, _) => updateStyle() }
  errorProperty.onChange { (_, _, _) => updateStyle() }
  correctProperty.onChange { (_, _, _) => updateStyle() }
  highlightedProperty.onChange { (_, _, _) => updateStyle() }
  conflictProperty.onChange { (_, _, _) => updateStyle() }
  relatedProperty.onChange { (_, _, _) => updateStyle() }
  
  /**
   * Fonction : applyBlockBorders
   * Role     : Applique les bordures epaisses pour delimiter les blocs 3x3.
   * Retour   : Unit.
   */
  private def applyBlockBorders(): Unit = {
    val topWidth = if (row == 0 || row == 3 || row == 6) 4.5 else 0.5
    val leftWidth = if (col == 0 || col == 3 || col == 6) 4.5 else 0.5
    val bottomWidth = if (row == 2 || row == 5 || row == 8) 4.5 else 0.5
    val rightWidth = if (col == 2 || col == 5 || col == 8) 4.5 else 0.5
    
    this.style = s"""
      -fx-border-color: #3B2F2F;
      -fx-border-width: ${topWidth} ${rightWidth} ${bottomWidth} ${leftWidth};
      -fx-background-color: transparent;
    """
  }
  
  /**
   * Fonction : updateStyle
   * Role     : Met a jour le style visuel selon l'etat de la cellule.
   * Retour   : Unit.
   */
  def updateStyle(): Unit = {
    val isFixed = fixedProperty.value
    val isSelected = selectedProperty.value
    val isError = errorProperty.value
    val isCorrect = correctProperty.value
    val isHighlighted = highlightedProperty.value
    val isConflict = conflictProperty.value
    val isRelated = relatedProperty.value
    
    // Couleur de fond par priorite
    val bgColor = if (isError) {
      Theme.CellError
    } else if (isConflict) {
      Theme.CellConflict
    } else if (isSelected) {
      Theme.CellSelected
    } else if (isRelated) {
      Theme.CellRelated
    } else if (isHighlighted) {
      Theme.CellHighlight
    } else if (isFixed) {
      Theme.CellFixed
    } else {
      Theme.CellBackground
    }
    bgRect.fill = bgColor
    
    // Bordure de selection
    if (isSelected) {
      bgRect.stroke = Theme.Accent
      bgRect.strokeWidth = 4.5
    } else {
      bgRect.stroke = Theme.BorderLight
      bgRect.strokeWidth = 0
    }
    
    // Couleur du texte
    val textColor = if (isFixed) {
      Theme.TextFixed
    } else if (isError) {
      Theme.TextError
    } else if (isCorrect) {
      Theme.TextCorrect
    } else {
      Theme.TextEditable
    }
    valueLabel.textFill = textColor
  }
  
  /**************************************************
   * --- METHODES D'ACCES ---
   **************************************************/
  
  /**
   * Fonction : setValue
   * Role     : Definit la valeur de la cellule.
   * Param    : v - valeur (0-9).
   * Retour   : Unit.
   */
  def setValue(v: Int): Unit = valueProperty.value = v
  
  /**
   * Fonction : getValue
   * Role     : Retourne la valeur de la cellule.
   * Retour   : Valeur actuelle.
   */
  def getValue: Int = valueProperty.value
  
  /**
   * Fonction : setFixed
   * Role     : Definit si la cellule est fixe.
   * Param    : f - true si fixe.
   * Retour   : Unit.
   */
  def setFixed(f: Boolean): Unit = fixedProperty.value = f
  
  /**
   * Fonction : isFixedCell
   * Role     : Verifie si la cellule est fixe.
   * Retour   : true si fixe.
   */
  def isFixedCell: Boolean = fixedProperty.value
  
  /**
   * Fonction : setSelected
   * Role     : Definit si la cellule est selectionnee.
   * Param    : s - true si selectionnee.
   * Retour   : Unit.
   */
  def setSelected(s: Boolean): Unit = selectedProperty.value = s
  
  /**
   * Fonction : isSelectedCell
   * Role     : Verifie si la cellule est selectionnee.
   * Retour   : true si selectionnee.
   */
  def isSelectedCell: Boolean = selectedProperty.value
  
  /**
   * Fonction : setError
   * Role     : Definit si la cellule est en erreur.
   * Param    : e - true si erreur.
   * Retour   : Unit.
   */
  def setError(e: Boolean): Unit = errorProperty.value = e
  
  /**
   * Fonction : hasError
   * Role     : Verifie si la cellule est en erreur.
   * Retour   : true si erreur.
   */
  def hasError: Boolean = errorProperty.value
  
  /**
   * Fonction : setCorrect
   * Role     : Definit si la valeur est correcte.
   * Param    : c - true si correcte.
   * Retour   : Unit.
   */
  def setCorrect(c: Boolean): Unit = correctProperty.value = c
  
  /**
   * Fonction : isCorrectCell
   * Role     : Verifie si la valeur est correcte.
   * Retour   : true si correcte.
   */
  def isCorrectCell: Boolean = correctProperty.value
  
  /**
   * Fonction : setHighlighted
   * Role     : Definit si la cellule est surlignee.
   * Param    : h - true si surlignee.
   * Retour   : Unit.
   */
  def setHighlighted(h: Boolean): Unit = highlightedProperty.value = h
  
  /**
   * Fonction : isHighlightedCell
   * Role     : Verifie si la cellule est surlignee.
   * Retour   : true si surlignee.
   */
  def isHighlightedCell: Boolean = highlightedProperty.value
  
  /**
   * Fonction : setConflict
   * Role     : Definit si la cellule est en conflit.
   * Param    : c - true si conflit.
   * Retour   : Unit.
   */
  def setConflict(c: Boolean): Unit = conflictProperty.value = c
  
  /**
   * Fonction : hasConflict
   * Role     : Verifie si la cellule est en conflit.
   * Retour   : true si conflit.
   */
  def hasConflict: Boolean = conflictProperty.value
  
  /**
   * Fonction : setRelated
   * Role     : Definit si la cellule est liee a la selection.
   * Param    : r - true si liee.
   * Retour   : Unit.
   */
  def setRelated(r: Boolean): Unit = relatedProperty.value = r
  
  /**
   * Fonction : isRelatedCell
   * Role     : Verifie si la cellule est liee a la selection.
   * Retour   : true si liee.
   */
  def isRelatedCell: Boolean = relatedProperty.value
  
  updateStyle()
}

/**
 * Objet compagnon definissant les constantes de CellView.
 */
object CellView {
  /** Taille d'une cellule en pixels */
  val Size: Double = 80.0
}
