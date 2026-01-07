/*
 * Nom     : src/main/scala/sudoku/ui/StatsPanel.scala
 * Role    : Panneau affichant les statistiques de la partie en cours.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.scene.layout.VBox
import scalafx.scene.control.Label
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight}

/**
 * Fonction : StatsPanel
 * Role     : Panneau affichant les statistiques de la partie en cours.
 * Param    : gameView - reference a la vue principale du jeu.
 */
class StatsPanel(gameView: GameView) extends VBox {
  
  spacing = 18
  padding = Insets(20)
  alignment = Pos.TopCenter
  prefWidth = 190
  style = "-fx-background-color: #F0E6E0; -fx-background-radius: 16;"
  
  /**************************************************
   * --- COMPOSANTS GRAPHIQUES ---
   **************************************************/
  
  /** Titre du panneau */
  private val titleLabel = new Label("Statistiques") {
    font = Font.font("Georgia", FontWeight.Bold, 22)
    textFill = Theme.Primary
  }
  
  /** Titre temps */
  private val timerTitleLabel = new Label("Temps") {
    font = Font.font("Georgia", FontWeight.Bold, 19)
    textFill = Theme.Text
  }
  
  /** Valeur temps */
  private val timerLabel = new Label("00:00") {
    font = Font.font("Georgia", FontWeight.Normal, 34)
    textFill = Theme.Accent
  }
  
  /** Titre erreurs */
  private val errorsTitleLabel = new Label("Erreurs") {
    font = Font.font("Georgia", FontWeight.Bold, 19)
    textFill = Theme.Text
  }
  
  /** Valeur erreurs */
  private val errorsLabel = new Label("0") {
    font = Font.font("Georgia", FontWeight.Normal, 34)
    textFill = Theme.Accent
  }
  
  /** Titre difficulte */
  private val difficultyTitleLabel = new Label("Difficulte") {
    font = Font.font("Georgia", FontWeight.Bold, 19)
    textFill = Theme.Text
  }
  
  /** Valeur difficulte */
  private val difficultyLabel = new Label("-") {
    font = Font.font("Georgia", FontWeight.Normal, 21)
    textFill = Theme.Primary
  }
  
  /** Titre progression */
  private val progressTitleLabel = new Label("Progression") {
    font = Font.font("Georgia", FontWeight.Bold, 19)
    textFill = Theme.Text
  }
  
  /** Valeur progression */
  private val progressLabel = new Label("0/81") {
    font = Font.font("Georgia", FontWeight.Normal, 21)
    textFill = Theme.Primary
  }
  
  /**
   * Fonction : spacer
   * Role     : Cree un espaceur vertical.
   * Retour   : Label vide avec hauteur definie.
   */
  private def spacer() = new Label("") { prefHeight = 10 }
  
  children = Seq(
    titleLabel,
    spacer(),
    timerTitleLabel,
    timerLabel,
    spacer(),
    errorsTitleLabel,
    errorsLabel,
    spacer(),
    difficultyTitleLabel,
    difficultyLabel,
    spacer(),
    progressTitleLabel,
    progressLabel
  )
  
  /**************************************************
   * --- BINDINGS ---
   **************************************************/
  
  gameView.elapsedTimeProperty.onChange { (_, _, newVal) =>
    val seconds = newVal.longValue()
    val m = seconds / 60
    val s = seconds % 60
    timerLabel.text = f"$m%02d:$s%02d"
  }
  
  gameView.errorCountProperty.onChange { (_, _, newVal) =>
    errorsLabel.text = newVal.toString
  }
  
  gameView.gameStateProperty.onChange { (_, _, newState) =>
    newState match {
      case Some(state) =>
        difficultyLabel.text = state.difficulty.name
        updateProgress(state)
      case None =>
        difficultyLabel.text = "-"
        progressLabel.text = "0/81"
    }
  }
  
  /**
   * Fonction : updateProgress
   * Role     : Met a jour l'affichage de la progression.
   * Param    : state - etat actuel de la partie.
   * Retour   : Unit.
   */
  private def updateProgress(state: sudoku.model.GameState): Unit = {
    var filled = 0
    for {
      r <- 0 until 9
      c <- 0 until 9
      if state.grid.getValue(r, c) != 0
    } filled += 1
    
    progressLabel.text = s"$filled/81"
  }
  
  /**
   * Fonction : refresh
   * Role     : Rafraichit l'affichage du panneau.
   * Retour   : Unit.
   */
  def refresh(): Unit = {
    gameView.gameStateProperty.value.foreach(updateProgress)
  }
}
