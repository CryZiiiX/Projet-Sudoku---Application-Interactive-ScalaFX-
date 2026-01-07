/*
 * Nom     : src/main/scala/sudoku/model/Action.scala
 * Role    : Definition des actions reversibles pour le systeme Undo/Redo.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.model

/**
 * Trait representant une action reversible sur la grille.
 */
sealed trait Action {
  def row: Int
  def col: Int
}

/**
 * Fonction : SetValueAction
 * Role     : Represente une action de placement d'une valeur dans une cellule.
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : oldValue - ancienne valeur avant modification.
 * Param    : newValue - nouvelle valeur apres modification.
 */
case class SetValueAction(row: Int, col: Int, oldValue: Int, newValue: Int) extends Action

/**
 * Fonction : ClearAction
 * Role     : Represente une action d'effacement d'une valeur.
 * Param    : row - ligne de la cellule.
 * Param    : col - colonne de la cellule.
 * Param    : oldValue - valeur effacee.
 */
case class ClearAction(row: Int, col: Int, oldValue: Int) extends Action

/**
 * Fonction : ActionHistory
 * Role     : Gestionnaire d'historique pour le systeme Undo/Redo.
 */
class ActionHistory {
  private var undoStack: List[Action] = Nil
  private var redoStack: List[Action] = Nil
  
  /**
   * Fonction : push
   * Role     : Ajoute une action a l'historique et efface le redo.
   * Param    : action - action a enregistrer.
   * Retour   : Unit.
   */
  def push(action: Action): Unit = {
    undoStack = action :: undoStack
    redoStack = Nil
  }
  
  /**
   * Fonction : canUndo
   * Role     : Verifie si une annulation est possible.
   * Retour   : true si l'historique contient des actions.
   */
  def canUndo: Boolean = undoStack.nonEmpty
  
  /**
   * Fonction : canRedo
   * Role     : Verifie si un retablissement est possible.
   * Retour   : true si des actions annulees existent.
   */
  def canRedo: Boolean = redoStack.nonEmpty
  
  /**
   * Fonction : undo
   * Role     : Annule la derniere action et la deplace vers le redo.
   * Retour   : Option contenant l'action annulee.
   */
  def undo(): Option[Action] = undoStack match {
    case action :: rest =>
      undoStack = rest
      redoStack = action :: redoStack
      Some(action)
    case Nil => None
  }
  
  /**
   * Fonction : redo
   * Role     : Retablit la derniere action annulee.
   * Retour   : Option contenant l'action retablie.
   */
  def redo(): Option[Action] = redoStack match {
    case action :: rest =>
      redoStack = rest
      undoStack = action :: undoStack
      Some(action)
    case Nil => None
  }
  
  /**
   * Fonction : clear
   * Role     : Vide completement l'historique.
   * Retour   : Unit.
   */
  def clear(): Unit = {
    undoStack = Nil
    redoStack = Nil
  }
  
  /**
   * Fonction : undoCount
   * Role     : Retourne le nombre d'actions annulables.
   * Retour   : Nombre d'actions dans la pile undo.
   */
  def undoCount: Int = undoStack.length
  
  /**
   * Fonction : redoCount
   * Role     : Retourne le nombre d'actions retablissables.
   * Retour   : Nombre d'actions dans la pile redo.
   */
  def redoCount: Int = redoStack.length
  
  /**
   * Fonction : toList
   * Role     : Convertit l'historique en liste pour serialisation.
   * Retour   : Liste des actions dans l'ordre chronologique.
   */
  def toList: List[Action] = undoStack.reverse
  
  /**
   * Fonction : loadFromList
   * Role     : Charge l'historique depuis une liste serialisee.
   * Param    : actions - liste des actions.
   * Param    : currentIndex - position actuelle dans l'historique.
   * Retour   : Unit.
   */
  def loadFromList(actions: List[Action], currentIndex: Int): Unit = {
    clear()
    undoStack = actions.take(currentIndex).reverse
    redoStack = actions.drop(currentIndex)
  }
}

/**
 * Objet compagnon pour la creation d'ActionHistory.
 */
object ActionHistory {
  /**
   * Fonction : apply
   * Role     : Cree une nouvelle instance d'ActionHistory.
   * Retour   : ActionHistory vide.
   */
  def apply(): ActionHistory = new ActionHistory()
}
