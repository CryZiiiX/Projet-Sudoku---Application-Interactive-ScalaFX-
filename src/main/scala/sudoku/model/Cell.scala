/*
 * Nom     : src/main/scala/sudoku/model/Cell.scala
 * Role    : Definition du modele de cellule pour la grille Sudoku.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.model

/**
 * Fonction : Cell
 * Role     : Represente une cellule du Sudoku avec sa valeur et son etat modifiable.
 * Param    : value - valeur de la cellule (0 = vide, 1-9 = valeur).
 * Param    : fixed - true si la cellule est fixe (puzzle original).
 */
case class Cell(value: Int, fixed: Boolean) {
  require(value >= 0 && value <= 9, s"Valeur invalide: $value")
  
  /**
   * Fonction : isEmpty
   * Role     : Verifie si la cellule est vide.
   * Retour   : true si la valeur est 0.
   */
  def isEmpty: Boolean = value == 0
  
  /**
   * Fonction : isSet
   * Role     : Verifie si la cellule contient une valeur.
   * Retour   : true si la valeur est differente de 0.
   */
  def isSet: Boolean = value != 0
  
  /**
   * Fonction : withValue
   * Role     : Cree une nouvelle cellule avec une valeur modifiee.
   * Param    : newValue - nouvelle valeur a assigner.
   * Retour   : Cell avec la nouvelle valeur.
   */
  def withValue(newValue: Int): Cell = {
    require(!fixed, "Impossible de modifier une cellule fixe")
    copy(value = newValue)
  }
  
  /**
   * Fonction : clear
   * Role     : Vide la cellule en mettant sa valeur a 0.
   * Retour   : Cell avec valeur 0.
   */
  def clear(): Cell = {
    require(!fixed, "Impossible de vider une cellule fixe")
    copy(value = 0)
  }
}

/**
 * Objet compagnon pour la creation de cellules.
 */
object Cell {
  /**
   * Fonction : empty
   * Role     : Cree une cellule vide et editable.
   * Retour   : Cell vide.
   */
  def empty: Cell = Cell(0, fixed = false)
  
  /**
   * Fonction : fixed
   * Role     : Cree une cellule fixe avec une valeur.
   * Param    : value - valeur de la cellule.
   * Retour   : Cell fixe.
   */
  def fixed(value: Int): Cell = Cell(value, fixed = true)
  
  /**
   * Fonction : editable
   * Role     : Cree une cellule editable avec une valeur.
   * Param    : value - valeur de la cellule.
   * Retour   : Cell editable.
   */
  def editable(value: Int): Cell = Cell(value, fixed = false)
}
