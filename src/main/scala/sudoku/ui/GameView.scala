/*
 * Nom     : src/main/scala/sudoku/ui/GameView.scala
 * Role    : Vue principale orchestrant les interactions du jeu Sudoku.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.Includes._
import scalafx.scene.layout.{BorderPane, VBox, HBox}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.{KeyEvent, KeyCode, MouseEvent}
import scalafx.geometry.{Insets, Pos}
import scalafx.beans.property.{ObjectProperty, BooleanProperty, IntegerProperty, LongProperty, StringProperty}
import scalafx.application.Platform
import sudoku.model._
import sudoku.logic._
import java.util.{Timer, TimerTask}

/**
 * Fonction : GameView
 * Role     : Vue principale orchestrant toutes les interactions du jeu.
 */
class GameView extends BorderPane {
  
  /**************************************************
   * --- PROPRIETES OBSERVABLES ---
   **************************************************/
  
  /** Etat actuel de la partie */
  val gameStateProperty: ObjectProperty[Option[GameState]] = ObjectProperty(None)
  
  /** Cellule selectionnee */
  val selectedCellProperty: ObjectProperty[Option[(Int, Int)]] = ObjectProperty(None)
  
  /** Mode aide actif */
  val highlightModeProperty: BooleanProperty = BooleanProperty(false)
  
  /** Valeur surlignee */
  val highlightValueProperty: IntegerProperty = IntegerProperty(0)
  
  /** Mode contraintes actif */
  val showConstraintsProperty: BooleanProperty = BooleanProperty(false)
  
  /** Temps ecoule en secondes */
  val elapsedTimeProperty: LongProperty = LongProperty(0)
  
  /** Nombre d'erreurs */
  val errorCountProperty: IntegerProperty = IntegerProperty(0)
  
  /** Message de statut */
  val statusProperty: StringProperty = StringProperty("Bienvenue dans Sudoku")
  
  /** Aide disponible */
  val helpAvailableProperty: BooleanProperty = BooleanProperty(true)
  
  style = "-fx-background-color: #FFF7F2;"
  
  /**************************************************
   * --- COMPOSANTS UI ---
   **************************************************/
  
  /** Grille de jeu */
  val sudokuGrid = new SudokuGridPane()
  
  /** Barre d'outils */
  val toolbar = new ToolbarView(this)
  
  /** Panneau de statistiques */
  val statsPanel = new StatsPanel(this)
  
  /** Timer de la partie */
  private var gameTimer: Option[Timer] = None
  private var gameStartTime: Long = 0
  
  /** Boutons du pave numerique */
  private val numPadButtons = scala.collection.mutable.ArrayBuffer[Button]()
  
  /**************************************************
   * --- LAYOUT ---
   **************************************************/
  
  padding = Insets(25)
  left = toolbar
  center = new VBox {
    spacing = 25
    alignment = Pos.Center
    padding = Insets(0, 25, 0, 25)
    children = Seq(sudokuGrid, createNumberPad())
  }
  right = statsPanel
  bottom = createStatusBar()
  
  setupGridHandlers()
  setupKeyboardHandlers()
  
  // Forcer l'application du style apres l'attachement a la scene
  Platform.runLater {
    updateNumPadStyle()
  }
  
  /**
   * Fonction : setupGridHandlers
   * Role     : Configure les handlers de clic sur la grille.
   * Retour   : Unit.
   */
  private def setupGridHandlers(): Unit = {
    sudokuGrid.getAllCells.foreach { cell =>
      cell.onMouseClicked = (event: MouseEvent) => {
        selectCell(cell.row, cell.col)
      }
    }
  }
  
  /**
   * Fonction : setupKeyboardHandlers
   * Role     : Configure les handlers clavier.
   * Retour   : Unit.
   */
  private def setupKeyboardHandlers(): Unit = {
    this.onKeyPressed = (event: KeyEvent) => {
      event.code match {
        case KeyCode.Digit1 | KeyCode.Numpad1 => enterValue(1)
        case KeyCode.Digit2 | KeyCode.Numpad2 => enterValue(2)
        case KeyCode.Digit3 | KeyCode.Numpad3 => enterValue(3)
        case KeyCode.Digit4 | KeyCode.Numpad4 => enterValue(4)
        case KeyCode.Digit5 | KeyCode.Numpad5 => enterValue(5)
        case KeyCode.Digit6 | KeyCode.Numpad6 => enterValue(6)
        case KeyCode.Digit7 | KeyCode.Numpad7 => enterValue(7)
        case KeyCode.Digit8 | KeyCode.Numpad8 => enterValue(8)
        case KeyCode.Digit9 | KeyCode.Numpad9 => enterValue(9)
        case KeyCode.BackSpace | KeyCode.Delete => clearSelectedCell()
        case KeyCode.Up => moveSelection(-1, 0)
        case KeyCode.Down => moveSelection(1, 0)
        case KeyCode.Left => moveSelection(0, -1)
        case KeyCode.Right => moveSelection(0, 1)
        case KeyCode.Z if event.isControlDown => undo()
        case KeyCode.Y if event.isControlDown => redo()
        case _ =>
      }
      event.consume()
    }
  }
  
  /**************************************************
   * --- STYLES DU PAVE NUMERIQUE ---
   **************************************************/
  
  private val numPadStyleNormal = """
    -fx-background-color: #3B2F2F;
    -fx-text-fill: #FFF7F2;
    -fx-font-weight: bold;
    -fx-font-size: 22;
    -fx-background-radius: 12;
    -fx-cursor: hand;
  """
  
  private val numPadStyleHover = """
    -fx-background-color: #FF7A59;
    -fx-text-fill: #FFF7F2;
    -fx-font-weight: bold;
    -fx-font-size: 22;
    -fx-background-radius: 12;
    -fx-cursor: hand;
  """
  
  private val numPadStyleHelp = """
    -fx-background-color: #FFD700;
    -fx-text-fill: #3B2F2F;
    -fx-font-weight: bold;
    -fx-font-size: 22;
    -fx-background-radius: 12;
    -fx-cursor: hand;
    -fx-border-color: #FF7A59;
    -fx-border-width: 3;
    -fx-border-radius: 12;
  """
  
  private val numPadStyleHelpHover = """
    -fx-background-color: #FFA500;
    -fx-text-fill: #3B2F2F;
    -fx-font-weight: bold;
    -fx-font-size: 22;
    -fx-background-radius: 12;
    -fx-cursor: hand;
    -fx-border-color: #FF7A59;
    -fx-border-width: 3;
    -fx-border-radius: 12;
  """
  
  /**
   * Fonction : createNumberPad
   * Role     : Cree le pave numerique 1-9.
   * Retour   : HBox contenant les boutons.
   */
  private def createNumberPad(): HBox = {
    new HBox {
      spacing = 12
      alignment = Pos.Center
      children = (1 to 9).map { n =>
        val btn = new Button(n.toString) {
          prefWidth = 70
          prefHeight = 70
          style = numPadStyleNormal
          onMouseEntered = _ => {
            if (highlightModeProperty.value) {
              style = numPadStyleHelpHover
            } else {
              style = numPadStyleHover
            }
          }
          onMouseExited = _ => {
            if (highlightModeProperty.value) {
              style = numPadStyleHelp
            } else {
              style = numPadStyleNormal
            }
          }
          onAction = _ => enterValue(n)
        }
        numPadButtons += btn
        btn
      }
    }
  }
  
  /**
   * Fonction : updateNumPadStyle
   * Role     : Met a jour le style du pave selon le mode aide.
   * Retour   : Unit.
   */
  private def updateNumPadStyle(): Unit = {
    val newStyle = if (highlightModeProperty.value) numPadStyleHelp else numPadStyleNormal
    numPadButtons.foreach(_.style = newStyle)
  }
  
  highlightModeProperty.onChange { (_, _, _) =>
    updateNumPadStyle()
  }
  
  /**
   * Fonction : createStatusBar
   * Role     : Cree la barre de statut en bas de l'ecran.
   * Retour   : HBox de la barre de statut.
   */
  private def createStatusBar(): HBox = {
    new HBox {
      padding = Insets(10, 5, 5, 5)
      alignment = Pos.CenterLeft
      children = Seq(
        new Label {
          text <== statusProperty
          style = "-fx-text-fill: #3B2F2F; -fx-font-style: italic;"
        }
      )
    }
  }
  
  /**************************************************
   * --- GESTION DE PARTIE ---
   **************************************************/
  
  /**
   * Fonction : startNewGame
   * Role     : Demarre une nouvelle partie avec la difficulte specifiee.
   * Param    : difficulty - niveau de difficulte.
   * Retour   : Unit.
   */
  def startNewGame(difficulty: Difficulty): Unit = {
    statusProperty.value = "Generation du puzzle..."
    
    new Thread(() => {
      val (puzzle, solution) = Generator.generate(difficulty)
      
      Platform.runLater {
        val state = GameState.create(
          puzzleId = java.util.UUID.randomUUID().toString,
          puzzle = puzzle,
          solution = solution,
          difficulty = difficulty
        )
        
        loadGameState(state)
        statusProperty.value = s"Nouveau puzzle ${difficulty.name} - Bonne chance!"
      }
    }).start()
  }
  
  /**
   * Fonction : loadGameState
   * Role     : Charge un etat de jeu existant.
   * Param    : state - etat a charger.
   * Retour   : Unit.
   */
  def loadGameState(state: GameState): Unit = {
    stopTimer()
    
    gameStateProperty.value = Some(state)
    sudokuGrid.updateFromGrid(state.grid)
    elapsedTimeProperty.value = state.elapsedSeconds
    errorCountProperty.value = state.errorCount
    helpAvailableProperty.value = !state.helpUsed
    highlightModeProperty.value = false
    
    selectedCellProperty.value = None
    sudokuGrid.clearSelection()
    sudokuGrid.clearErrors()
    sudokuGrid.clearHighlights()
    sudokuGrid.clearConflicts()
    sudokuGrid.clearRelated()
    
    startTimer()
    this.requestFocus()
  }
  
  /**************************************************
   * --- SELECTION ET SAISIE ---
   **************************************************/
  
  /**
   * Fonction : selectCell
   * Role     : Selectionne une cellule de la grille.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Unit.
   */
  def selectCell(row: Int, col: Int): Unit = {
    sudokuGrid.clearSelection()
    sudokuGrid.clearConflicts()
    sudokuGrid.clearRelated()
    
    val cell = sudokuGrid.getCell(row, col)
    cell.setSelected(true)
    selectedCellProperty.value = Some((row, col))
    
    if (highlightModeProperty.value && cell.getValue > 0) {
      highlightValueProperty.value = cell.getValue
      sudokuGrid.highlightValue(cell.getValue)
    }
    
    if (showConstraintsProperty.value) {
      showConstraintsForCell(row, col)
    }
    
    this.requestFocus()
  }
  
  /**
   * Fonction : showConstraintsForCell
   * Role     : Affiche les contraintes pour une cellule.
   * Param    : row - ligne de la cellule.
   * Param    : col - colonne de la cellule.
   * Retour   : Unit.
   */
  private def showConstraintsForCell(row: Int, col: Int): Unit = {
    sudokuGrid.showRelatedCells(row, col)
    
    gameStateProperty.value.foreach { state =>
      val value = state.grid.getValue(row, col)
      if (value > 0) {
        val conflicts = Validator.getConflictingCells(state.grid, row, col)
        sudokuGrid.showConflicts(conflicts)
      }
    }
  }
  
  /**
   * Fonction : moveSelection
   * Role     : Deplace la selection avec les fleches.
   * Param    : dRow - deplacement en ligne.
   * Param    : dCol - deplacement en colonne.
   * Retour   : Unit.
   */
  private def moveSelection(dRow: Int, dCol: Int): Unit = {
    selectedCellProperty.value.foreach { case (row, col) =>
      val newRow = (row + dRow + 9) % 9
      val newCol = (col + dCol + 9) % 9
      selectCell(newRow, newCol)
    }
  }
  
  /**
   * Fonction : enterValue
   * Role     : Entre une valeur dans la cellule selectionnee.
   * Param    : value - valeur a entrer (1-9).
   * Retour   : Unit.
   */
  def enterValue(value: Int): Unit = {
    if (highlightModeProperty.value) {
      useHelpForValue(value)
      return
    }
    
    (gameStateProperty.value, selectedCellProperty.value) match {
      case (Some(state), Some((row, col))) if !state.grid.isFixed(row, col) =>
        val newState = state.setValue(row, col, value)
        gameStateProperty.value = Some(newState)
        
        val cell = sudokuGrid.getCell(row, col)
        cell.setValue(value)
        
        if (value == 0) {
          cell.setError(false)
          cell.setCorrect(false)
        } else {
          val isCorrect = newState.solution.getValue(row, col) == value
          cell.setError(!isCorrect)
          cell.setCorrect(isCorrect)
          
          if (!isCorrect) {
            errorCountProperty.value = newState.errorCount
            statusProperty.value = "Erreur! Ce chiffre ne correspond pas a la solution."
          } else {
            statusProperty.value = "Correct!"
          }
        }
        
        if (newState.completed) {
          stopTimer()
          statusProperty.value = s"Felicitations! Puzzle resolu en ${formatTime(elapsedTimeProperty.value)} avec ${newState.errorCount} erreur(s)."
        }
        
      case _ =>
    }
  }
  
  /**
   * Fonction : useHelpForValue
   * Role     : Utilise l'aide pour surligner un chiffre.
   * Param    : value - chiffre a surligner.
   * Retour   : Unit.
   */
  private def useHelpForValue(value: Int): Unit = {
    highlightValueProperty.value = value
    sudokuGrid.highlightValue(value)
    
    gameStateProperty.value.foreach { state =>
      val newState = state.useHelp()
      gameStateProperty.value = Some(newState)
      helpAvailableProperty.value = false
    }
    
    highlightModeProperty.value = false
    statusProperty.value = s"Aide utilisee : affichage du chiffre $value"
  }
  
  /**
   * Fonction : clearSelectedCell
   * Role     : Efface la valeur de la cellule selectionnee.
   * Retour   : Unit.
   */
  def clearSelectedCell(): Unit = {
    (gameStateProperty.value, selectedCellProperty.value) match {
      case (Some(state), Some((row, col))) if !state.grid.isFixed(row, col) =>
        val newState = state.clearCell(row, col)
        gameStateProperty.value = Some(newState)
        
        val cell = sudokuGrid.getCell(row, col)
        cell.setValue(0)
        cell.setError(false)
        cell.setCorrect(false)
        statusProperty.value = ""
        
      case _ =>
    }
  }
  
  /**************************************************
   * --- UNDO/REDO ---
   **************************************************/
  
  /**
   * Fonction : undo
   * Role     : Annule la derniere action.
   * Retour   : Unit.
   */
  def undo(): Unit = {
    gameStateProperty.value.foreach { state =>
      if (state.canUndo) {
        val newState = state.undo()
        gameStateProperty.value = Some(newState)
        sudokuGrid.updateFromGrid(newState.grid)
        refreshErrors()
        statusProperty.value = "Action annulee"
      }
    }
  }
  
  /**
   * Fonction : redo
   * Role     : Retablit la derniere action annulee.
   * Retour   : Unit.
   */
  def redo(): Unit = {
    gameStateProperty.value.foreach { state =>
      if (state.canRedo) {
        val newState = state.redo()
        gameStateProperty.value = Some(newState)
        sudokuGrid.updateFromGrid(newState.grid)
        refreshErrors()
        statusProperty.value = "Action retablie"
      }
    }
  }
  
  /**
   * Fonction : refreshErrors
   * Role     : Rafraichit l'affichage des erreurs sur la grille.
   * Retour   : Unit.
   */
  private def refreshErrors(): Unit = {
    gameStateProperty.value.foreach { state =>
      sudokuGrid.clearErrors()
      sudokuGrid.clearCorrect()
      for {
        r <- 0 until 9
        c <- 0 until 9
        value = state.grid.getValue(r, c)
        if value > 0 && !state.grid.isFixed(r, c)
      } {
        val isCorrect = value == state.solution.getValue(r, c)
        sudokuGrid.setError(r, c, !isCorrect)
        sudokuGrid.setCorrect(r, c, isCorrect)
      }
    }
  }
  
  /**************************************************
   * --- MODES D'AFFICHAGE ---
   **************************************************/
  
  /**
   * Fonction : toggleHighlightMode
   * Role     : Active/desactive le mode aide.
   * Retour   : Unit.
   */
  def toggleHighlightMode(): Unit = {
    gameStateProperty.value match {
      case Some(state) if state.helpUsed =>
        statusProperty.value = "Aide deja utilisee pour cette partie!"
        highlightModeProperty.value = false
      case Some(_) =>
        highlightModeProperty.value = !highlightModeProperty.value
        if (highlightModeProperty.value) {
          statusProperty.value = "Mode aide actif - Cliquez sur un chiffre pour le surligner"
        } else {
          sudokuGrid.clearHighlights()
          highlightValueProperty.value = 0
          statusProperty.value = ""
        }
      case None =>
        statusProperty.value = "Aucune partie en cours"
    }
  }
  
  /**
   * Fonction : toggleShowConstraints
   * Role     : Active/desactive l'affichage des contraintes.
   * Retour   : Unit.
   */
  def toggleShowConstraints(): Unit = {
    showConstraintsProperty.value = !showConstraintsProperty.value
    if (!showConstraintsProperty.value) {
      sudokuGrid.clearConflicts()
      sudokuGrid.clearRelated()
    } else {
      selectedCellProperty.value.foreach { case (r, c) =>
        showConstraintsForCell(r, c)
      }
    }
  }
  
  /**
   * Fonction : highlightNumber
   * Role     : Surligne toutes les occurrences d'un chiffre.
   * Param    : value - chiffre a surligner.
   * Retour   : Unit.
   */
  def highlightNumber(value: Int): Unit = {
    highlightValueProperty.value = value
    sudokuGrid.highlightValue(value)
  }
  
  /**************************************************
   * --- TIMER ---
   **************************************************/
  
  /**
   * Fonction : startTimer
   * Role     : Demarre le timer de la partie.
   * Retour   : Unit.
   */
  private def startTimer(): Unit = {
    stopTimer()
    gameStartTime = System.currentTimeMillis() - (elapsedTimeProperty.value * 1000)
    
    val timer = new Timer(true)
    timer.scheduleAtFixedRate(new TimerTask {
      def run(): Unit = {
        val elapsed = (System.currentTimeMillis() - gameStartTime) / 1000
        Platform.runLater {
          elapsedTimeProperty.value = elapsed
          gameStateProperty.value.foreach { state =>
            gameStateProperty.value = Some(state.updateElapsed(elapsed))
          }
        }
      }
    }, 0, 1000)
    
    gameTimer = Some(timer)
  }
  
  /**
   * Fonction : stopTimer
   * Role     : Arrete le timer de la partie.
   * Retour   : Unit.
   */
  private def stopTimer(): Unit = {
    gameTimer.foreach(_.cancel())
    gameTimer = None
  }
  
  /**
   * Fonction : formatTime
   * Role     : Formate le temps en MM:SS.
   * Param    : seconds - temps en secondes.
   * Retour   : Chaine formatee.
   */
  private def formatTime(seconds: Long): String = {
    val m = seconds / 60
    val s = seconds % 60
    f"$m%02d:$s%02d"
  }
  
  /**************************************************
   * --- ACCESSEURS ---
   **************************************************/
  
  /**
   * Fonction : getCurrentState
   * Role     : Retourne l'etat actuel de la partie.
   * Retour   : Option contenant l'etat.
   */
  def getCurrentState: Option[GameState] = gameStateProperty.value
  
  /**
   * Fonction : getStage
   * Role     : Retourne la fenetre parente.
   * Retour   : Option contenant le Stage.
   */
  def getStage: Option[scalafx.stage.Stage] = {
    Option(this.scene.value).flatMap(s => Option(s.getWindow)).map(w => new scalafx.stage.Stage(w.asInstanceOf[javafx.stage.Stage]))
  }
  
  /**
   * Fonction : cleanup
   * Role     : Nettoie les ressources avant fermeture.
   * Retour   : Unit.
   */
  def cleanup(): Unit = {
    stopTimer()
  }
}
