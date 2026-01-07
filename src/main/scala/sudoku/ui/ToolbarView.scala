/*
 * Nom     : src/main/scala/sudoku/ui/ToolbarView.scala
 * Role    : Barre d'outils verticale avec les boutons d'action du jeu.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.Includes._
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Button, Separator, ToggleButton, ChoiceDialog}
import scalafx.scene.shape.Rectangle
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import sudoku.model.Difficulty
import sudoku.persistence.{GameRepository, PuzzleCatalog}
import java.io.File

/**
 * Fonction : ToolbarView
 * Role     : Barre d'outils verticale contenant les boutons d'action.
 * Param    : gameView - reference a la vue principale du jeu.
 */
class ToolbarView(gameView: GameView) extends VBox {
  
  spacing = 12
  padding = Insets(20)
  alignment = Pos.TopCenter
  prefWidth = 190
  style = "-fx-background-color: #F0E6E0; -fx-background-radius: 16;"
  
  /**
   * Fonction : applyButtonStyle
   * Role     : Applique le style visuel a un bouton normal.
   * Param    : btn - bouton a styliser.
   * Retour   : Unit.
   */
  private def applyButtonStyle(btn: Button): Unit = {
    btn.style = Theme.buttonStyle
    btn.onMouseEntered = _ => btn.style = Theme.buttonHoverStyle
    btn.onMouseExited = _ => btn.style = Theme.buttonStyle
  }
  
  /**
   * Fonction : applyToggleStyle
   * Role     : Applique le style visuel a un bouton toggle.
   * Param    : btn - bouton toggle a styliser.
   * Retour   : Unit.
   */
  private def applyToggleStyle(btn: ToggleButton): Unit = {
    btn.style = Theme.buttonStyle
    btn.selectedProperty.onChange { (_, _, isSelected) =>
      if (isSelected) {
        btn.style = Theme.toggleButtonSelectedStyle
      } else {
        btn.style = Theme.buttonStyle
      }
    }
  }
  
  /**************************************************
   * --- BOUTONS ---
   **************************************************/
  
  /** Bouton nouvelle partie */
  private val newGameBtn = new Button("Nouveau") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(newGameBtn)
  newGameBtn.onAction = _ => showNewGameDialog()
  
  /** Bouton catalogue */
  private val catalogBtn = new Button("Catalogue") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(catalogBtn)
  catalogBtn.onAction = _ => showCatalogDialog()
  
  /** Bouton charger */
  private val loadBtn = new Button("Charger") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(loadBtn)
  loadBtn.onAction = _ => loadGame()
  
  /** Bouton sauvegarder */
  private val saveBtn = new Button("Sauvegarder") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(saveBtn)
  saveBtn.onAction = _ => saveGame()
  
  /** Bouton annuler */
  private val undoBtn = new Button("Annuler") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(undoBtn)
  undoBtn.onAction = _ => gameView.undo()
  
  /** Bouton refaire */
  private val redoBtn = new Button("Refaire") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(redoBtn)
  redoBtn.onAction = _ => gameView.redo()
  
  /** Bouton effacer */
  private val clearBtn = new Button("Effacer") {
    prefWidth = 170
    prefHeight = 55
  }
  applyButtonStyle(clearBtn)
  clearBtn.onAction = _ => gameView.clearSelectedCell()
  
  /** Bouton aide (toggle) */
  private val hintBtn = new ToggleButton("Aide (1/1)") {
    prefWidth = 170
    prefHeight = 55
  }
  applyToggleStyle(hintBtn)
  hintBtn.onAction = _ => {
    gameView.toggleHighlightMode()
    hintBtn.selected = gameView.highlightModeProperty.value
  }
  
  /** Bouton contraintes (toggle) */
  private val constraintsBtn = new ToggleButton("Contraintes") {
    prefWidth = 170
    prefHeight = 55
  }
  applyToggleStyle(constraintsBtn)
  constraintsBtn.onAction = _ => gameView.toggleShowConstraints()
  
  /**
   * Fonction : styledSeparator
   * Role     : Cree un separateur stylise.
   * Retour   : Separator stylise.
   */
  private def styledSeparator() = new Separator {
    style = "-fx-background-color: #D4C4BC;"
  }
  
  children = Seq(
    newGameBtn,
    catalogBtn,
    styledSeparator(),
    loadBtn,
    saveBtn,
    styledSeparator(),
    undoBtn,
    redoBtn,
    clearBtn,
    styledSeparator(),
    hintBtn,
    constraintsBtn
  )
  
  /**************************************************
   * --- BINDINGS ---
   **************************************************/
  
  gameView.gameStateProperty.onChange { (_, _, newState) =>
    val hasGame = newState.isDefined
    saveBtn.disable = !hasGame
    undoBtn.disable = !hasGame || !newState.exists(_.canUndo)
    redoBtn.disable = !hasGame || !newState.exists(_.canRedo)
    clearBtn.disable = !hasGame
    
    newState match {
      case Some(state) =>
        if (state.helpUsed) {
          hintBtn.text = "Aide (0/1)"
          hintBtn.disable = true
          hintBtn.selected = false
          hintBtn.style = """
            -fx-background-color: #A09090;
            -fx-text-fill: #E0D0D0;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
          """
        } else {
          hintBtn.text = "Aide (1/1)"
          hintBtn.disable = false
          applyToggleStyle(hintBtn)
        }
      case None =>
        hintBtn.text = "Aide (1/1)"
        hintBtn.disable = true
    }
  }
  
  gameView.highlightModeProperty.onChange { (_, _, isActive) =>
    hintBtn.selected = isActive
  }
  
  gameView.helpAvailableProperty.onChange { (_, _, isAvailable) =>
    if (!isAvailable) {
      hintBtn.text = "Aide (0/1)"
      hintBtn.disable = true
      hintBtn.selected = false
      hintBtn.style = """
        -fx-background-color: #A09090;
        -fx-text-fill: #E0D0D0;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
      """
    }
  }
  
  /**************************************************
   * --- DIALOGUES ---
   **************************************************/
  
  /**
   * Fonction : showNewGameDialog
   * Role     : Affiche le dialogue de selection de difficulte.
   * Retour   : Unit.
   */
  private def showNewGameDialog(): Unit = {
    val choices = Difficulty.all.map(_.name)
    val dialog = new ChoiceDialog(choices.head, choices) {
      title = "Nouvelle partie"
      headerText = "Choisissez la difficulte"
      contentText = "Difficulte:"
    }
    
    dialog.showAndWait() match {
      case Some(choice) =>
        val difficulty = Difficulty.fromString(choice)
        gameView.startNewGame(difficulty)
      case None =>
    }
  }
  
  /**
   * Fonction : showCatalogDialog
   * Role     : Affiche le dialogue de selection du catalogue.
   * Retour   : Unit.
   */
  private def showCatalogDialog(): Unit = {
    val selector = new PuzzleSelector(gameView)
    selector.show()
  }
  
  /**
   * Fonction : loadGame
   * Role     : Charge une partie sauvegardee via dialogue fichier.
   * Retour   : Unit.
   */
  private def loadGame(): Unit = {
    val fileChooser = new FileChooser {
      title = "Charger une partie"
      extensionFilters.add(new ExtensionFilter("Sauvegardes Sudoku", "*.json"))
    }
    
    val savesDir = new File(GameRepository.savesDirectory)
    savesDir.mkdirs()
    if (savesDir.exists()) {
      fileChooser.initialDirectory = savesDir
    }
    
    gameView.getStage.foreach { stage =>
      val selectedFile = fileChooser.showOpenDialog(stage)
      if (selectedFile != null) {
        GameRepository.load(selectedFile.getAbsolutePath) match {
          case Some(state) =>
            gameView.loadGameState(state)
            gameView.statusProperty.value = "Partie chargee"
          case None =>
            gameView.statusProperty.value = "Erreur lors du chargement"
        }
      }
    }
  }
  
  /**
   * Fonction : saveGame
   * Role     : Sauvegarde la partie actuelle via dialogue fichier.
   * Retour   : Unit.
   */
  private def saveGame(): Unit = {
    gameView.getCurrentState.foreach { state =>
      val fileChooser = new FileChooser {
        title = "Sauvegarder la partie"
        extensionFilters.add(new ExtensionFilter("Sauvegardes Sudoku", "*.json"))
        initialFileName = s"sudoku_${state.id.take(8)}.json"
      }
      
      val savesDir = new File(GameRepository.savesDirectory)
      savesDir.mkdirs()
      if (savesDir.exists()) {
        fileChooser.initialDirectory = savesDir
      }
      
      gameView.getStage.foreach { stage =>
        val selectedFile = fileChooser.showSaveDialog(stage)
        if (selectedFile != null) {
          val path = if (selectedFile.getName.endsWith(".json")) {
            selectedFile.getAbsolutePath
          } else {
            selectedFile.getAbsolutePath + ".json"
          }
          
          if (GameRepository.save(state, path)) {
            gameView.statusProperty.value = "Partie sauvegardee"
          } else {
            gameView.statusProperty.value = "Erreur lors de la sauvegarde"
          }
        }
      }
    }
  }
}
