/*
 * Nom     : src/main/scala/sudoku/ui/Theme.scala
 * Role    : Definition du theme visuel Warm Modern pour l'interface.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.ui.MainApp
 */
package sudoku.ui

import scalafx.scene.paint.Color

/**
 * Objet definissant le theme visuel Warm Modern.
 */
object Theme {
  
  /**************************************************
   * --- COULEURS PRINCIPALES ---
   **************************************************/
  
  /** Couleur primaire - Brun fonce */
  val Primary: Color = Color.web("#3B2F2F")
  
  /** Couleur d'accent - Orange corail */
  val Accent: Color = Color.web("#FF7A59")
  
  /** Couleur de fond - Beige clair/creme */
  val Background: Color = Color.web("#FFF7F2")
  
  /** Couleur de texte - Brun tres fonce */
  val Text: Color = Color.web("#2B1F1F")
  
  /**************************************************
   * --- COULEURS DERIVEES ---
   **************************************************/
  
  /** Primaire clair */
  val PrimaryLight: Color = Color.web("#5D4E4E")
  
  /** Primaire fonce */
  val PrimaryDark: Color = Color.web("#2A2020")
  
  /** Accent clair */
  val AccentLight: Color = Color.web("#FF9A7F")
  
  /** Accent fonce */
  val AccentDark: Color = Color.web("#E05A39")
  
  /**************************************************
   * --- COULEURS DES CELLULES ---
   **************************************************/
  
  /** Fond de cellule normale */
  val CellBackground: Color = Color.web("#FFFCFA")
  
  /** Fond de cellule fixe */
  val CellFixed: Color = Color.web("#F0E6E0")
  
  /** Fond de cellule selectionnee */
  val CellSelected: Color = Color.web("#FFE4D6")
  
  /** Fond de cellule surlignee */
  val CellHighlight: Color = Color.web("#FFF0E0")
  
  /** Fond de cellule en erreur */
  val CellError: Color = Color.web("#FFD4CC")
  
  /** Fond de cellule en conflit */
  val CellConflict: Color = Color.web("#FFE8D0")
  
  /** Fond de cellule liee - Bleu pale pour les contraintes */
  val CellRelated: Color = Color.web("#E6F3FF")
  
  /**************************************************
   * --- COULEURS DE TEXTE ---
   **************************************************/
  
  /** Texte des cellules fixes */
  val TextFixed: Color = Primary
  
  /** Texte des cellules editables */
  val TextEditable: Color = Accent
  
  /** Texte des valeurs correctes - Vert */
  val TextCorrect: Color = Color.web("#2E8B57")
  
  /** Texte des erreurs - Rouge */
  val TextError: Color = Color.web("#CC4433")
  
  /**************************************************
   * --- COULEURS DE BORDURE ---
   **************************************************/
  
  /** Bordure claire */
  val BorderLight: Color = Color.web("#D4C4BC")
  
  /** Bordure foncee */
  val BorderDark: Color = Primary
  
  /** Bordure de selection */
  val BorderSelected: Color = Accent
  
  /**************************************************
   * --- COULEURS DES BOUTONS ---
   **************************************************/
  
  /** Fond de bouton */
  val ButtonBackground: Color = Primary
  
  /** Texte de bouton */
  val ButtonText: Color = Color.web("#FFF7F2")
  
  /** Fond de bouton au survol */
  val ButtonHover: Color = PrimaryLight
  
  /**************************************************
   * --- STYLES CSS ---
   **************************************************/
  
  /** Style CSS pour les boutons normaux */
  val buttonStyle: String = s"""
    -fx-background-color: #3B2F2F;
    -fx-text-fill: #FFF7F2;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-cursor: hand;
  """
  
  /** Style CSS pour les boutons au survol */
  val buttonHoverStyle: String = s"""
    -fx-background-color: #5D4E4E;
    -fx-text-fill: #FFF7F2;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-cursor: hand;
  """
  
  /** Style CSS pour les boutons toggle selectionnes */
  val toggleButtonSelectedStyle: String = s"""
    -fx-background-color: #FF7A59;
    -fx-text-fill: #FFF7F2;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-cursor: hand;
  """
  
  /** Style CSS pour les panneaux */
  val panelStyle: String = s"""
    -fx-background-color: #FFF7F2;
    -fx-background-radius: 12;
  """
  
  /** Style CSS pour les labels */
  val labelStyle: String = s"""
    -fx-text-fill: #2B1F1F;
  """
  
  /** Style CSS pour les titres */
  val titleStyle: String = s"""
    -fx-text-fill: #3B2F2F;
    -fx-font-weight: bold;
  """
}
