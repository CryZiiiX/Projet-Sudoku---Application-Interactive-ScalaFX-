/*
 * Nom     : src/main/scala/sudoku/ui/PuzzleSelector.scala
 * Role    : Dialogue de selection de puzzle depuis le catalogue.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, HBox, BorderPane}
import scalafx.scene.control.{Button, Label, ListView, ChoiceBox}
import scalafx.stage.{Stage, Modality}
import scalafx.geometry.{Insets, Pos}
import scalafx.collections.ObservableBuffer
import scalafx.scene.text.{Font, FontWeight}
import sudoku.model.{Difficulty, GameState}
import sudoku.persistence.PuzzleCatalog

/**
 * Fonction : PuzzleSelector
 * Role     : Dialogue de selection de puzzle depuis le catalogue.
 * Param    : gameView - reference a la vue principale du jeu.
 */
class PuzzleSelector(gameView: GameView) {
  
  /** Liste des puzzles */
  private val puzzleList = new ListView[String] {
    style = "-fx-background-color: #FFFCFA; -fx-background-radius: 8;"
  }
  private val puzzleBuffer = ObservableBuffer[String]()
  puzzleList.items = puzzleBuffer
  
  /** Fenetre du dialogue */
  private val stage = new Stage {
    title = "Catalogue de Puzzles"
    initModality(Modality.ApplicationModal)
    width = 550
    height = 650
    
    scene = new Scene {
      fill = Theme.Background
      root = new BorderPane {
        padding = Insets(25)
        style = "-fx-background-color: #FFF7F2;"
        
        top = new VBox {
          spacing = 18
          children = Seq(
            new Label("Catalogue de Puzzles") {
              font = Font.font("Georgia", FontWeight.Bold, 24)
              textFill = Theme.Primary
            },
            new HBox {
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                new Label("Generer nouveau:") {
                  textFill = Theme.Text
                },
                createDifficultyChoice(),
                createGenerateButton()
              )
            }
          )
        }
        
        center = new VBox {
          spacing = 12
          padding = Insets(18, 0, 18, 0)
          children = Seq(
            new Label("Puzzles disponibles:") {
              font = Font.font("Georgia", FontWeight.Bold, 16)
              textFill = Theme.Text
            },
            puzzleList
          )
        }
        
        bottom = new HBox {
          spacing = 12
          alignment = Pos.CenterRight
          padding = Insets(12, 0, 0, 0)
          children = Seq(
            createSelectButton(),
            createCancelButton()
          )
        }
      }
    }
  }
  
  /** Difficulte selectionnee */
  private var selectedDifficulty: Difficulty = Difficulty.Medium
  
  /**
   * Fonction : createDifficultyChoice
   * Role     : Cree le selecteur de difficulte.
   * Retour   : ChoiceBox des difficultes.
   */
  private def createDifficultyChoice(): ChoiceBox[String] = {
    val choices = ObservableBuffer(Difficulty.all.map(_.name): _*)
    new ChoiceBox[String] {
      items = choices
      value = Difficulty.Medium.name
      style = "-fx-background-color: #F0E6E0;"
      value.onChange { (_, _, newVal) =>
        selectedDifficulty = Difficulty.fromString(newVal)
      }
    }
  }
  
  /**
   * Fonction : createGenerateButton
   * Role     : Cree le bouton de generation de puzzle.
   * Retour   : Button de generation.
   */
  private def createGenerateButton(): Button = {
    new Button("Generer") {
      style = Theme.buttonStyle
      onMouseEntered = _ => style = Theme.buttonHoverStyle
      onMouseExited = _ => style = Theme.buttonStyle
      onAction = _ => generateNewPuzzle()
    }
  }
  
  /**
   * Fonction : createSelectButton
   * Role     : Cree le bouton de selection pour jouer.
   * Retour   : Button de selection.
   */
  private def createSelectButton(): Button = {
    new Button("Jouer") {
      prefWidth = 100
      prefHeight = 40
      style = Theme.toggleButtonSelectedStyle
      onAction = _ => playSelectedPuzzle()
    }
  }
  
  /**
   * Fonction : createCancelButton
   * Role     : Cree le bouton de fermeture du dialogue.
   * Retour   : Button de fermeture.
   */
  private def createCancelButton(): Button = {
    new Button("Fermer") {
      prefWidth = 100
      prefHeight = 40
      style = Theme.buttonStyle
      onMouseEntered = _ => style = Theme.buttonHoverStyle
      onMouseExited = _ => style = Theme.buttonStyle
      onAction = _ => stage.close()
    }
  }
  
  /**
   * Fonction : loadCatalog
   * Role     : Charge et affiche les puzzles du catalogue.
   * Retour   : Unit.
   */
  private def loadCatalog(): Unit = {
    puzzleBuffer.clear()
    val catalog = PuzzleCatalog.loadCatalog()
    catalog.puzzles.foreach { entry =>
      val info = s"${entry.id.take(8)} - ${entry.difficulty} - ${entry.createdAt.take(10)}"
      puzzleBuffer.add(info)
    }
    
    if (puzzleBuffer.isEmpty) {
      puzzleBuffer.add("(Catalogue vide - generez un puzzle)")
    }
  }
  
  /**
   * Fonction : generateNewPuzzle
   * Role     : Genere un nouveau puzzle et l'ajoute au catalogue.
   * Retour   : Unit.
   */
  private def generateNewPuzzle(): Unit = {
    gameView.statusProperty.value = "Generation en cours..."
    
    new Thread(() => {
      val entry = PuzzleCatalog.generateAndAdd(selectedDifficulty)
      
      scalafx.application.Platform.runLater {
        loadCatalog()
        gameView.statusProperty.value = "Puzzle genere et ajoute au catalogue"
      }
    }).start()
  }
  
  /**
   * Fonction : playSelectedPuzzle
   * Role     : Lance le puzzle selectionne dans la liste.
   * Retour   : Unit.
   */
  private def playSelectedPuzzle(): Unit = {
    val selectedIdx = puzzleList.selectionModel.value.getSelectedIndex
    if (selectedIdx >= 0) {
      val catalog = PuzzleCatalog.loadCatalog()
      if (selectedIdx < catalog.puzzles.length) {
        val entry = catalog.puzzles(selectedIdx)
        val state = PuzzleCatalog.createGameState(entry)
        gameView.loadGameState(state)
        stage.close()
      }
    }
  }
  
  /**
   * Fonction : show
   * Role     : Affiche le dialogue de selection.
   * Retour   : Unit.
   */
  def show(): Unit = {
    loadCatalog()
    stage.show()
  }
}
