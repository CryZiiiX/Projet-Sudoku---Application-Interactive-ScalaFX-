/*
 * Nom     : src/main/scala/sudoku/ui/MainApp.scala
 * Role    : Point d'entree de l'application ScalaFX Sudoku.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import sudoku.model.Difficulty
import sudoku.persistence.PuzzleCatalog

/**
 * Objet principal de l'application Sudoku ScalaFX.
 */
object MainApp extends JFXApp3 {
  
  /**
   * Fonction : start
   * Role     : Point d'entree de l'application JavaFX.
   * Retour   : Unit.
   */
  override def start(): Unit = {
    PuzzleCatalog.initializeWithDefaults()
    
    val gameView = new GameView()
    
    stage = new JFXApp3.PrimaryStage {
      title = "Sudoku"
      width = 1400
      height = 1000
      scene = new Scene {
        fill = Theme.Background
        root = gameView
        root.value.requestFocus()
      }
      
      onCloseRequest = _ => {
        gameView.cleanup()
      }
    }
    
    gameView.startNewGame(Difficulty.Easy)
  }
}
