/*
 * Nom     : src/main/scala/sudoku/Main.scala
 * Role    : Point d'entree principal de l'application Sudoku.
 * Auteur  : Maxime BRONNY
 * Version : V1
 * Licence : Realise dans le cadre du cours Programmation concurrente - M1 Informatique Big Data
 * Usage   :
 *   - Compilation : scala-cli compile src/
 *   - Execution   : scala-cli run src/ --main-class sudoku.Main
 */
package sudoku

import sudoku.ui.MainApp

/**
 * Fonction : main
 * Role     : Point d'entree de l'application Sudoku.
 * Param    : args - arguments de ligne de commande.
 * Retour   : Unit.
 */
object Main {
  def main(args: Array[String]): Unit = {
    MainApp.main(args)
  }
}
