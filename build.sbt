// Sudoku ScalaFX - Projet SBT
// Lancement: sbt run
// Tests: sbt test
// Compilation: sbt compile

name := "sudoku-scalafx"
version := "1.0.0"
scalaVersion := "2.13.12"

// ScalaFX dependencies
val javaFXVersion = "21"
val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "org.openjfx" % "javafx-base" % javaFXVersion classifier osName,
  "org.openjfx" % "javafx-controls" % javaFXVersion classifier osName,
  "org.openjfx" % "javafx-graphics" % javaFXVersion classifier osName,
  "org.openjfx" % "javafx-media" % javaFXVersion classifier osName,
  
  // JSON serialization
  "com.lihaoyi" %% "upickle" % "3.1.3",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

// Fork JVM for JavaFX
fork := true

// Scala compiler options
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked"
)

