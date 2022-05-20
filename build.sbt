ThisBuild / scalaVersion     := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "tagless-final-example",
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-core"            % "2.7.0",
      "org.scalatest"     %% "scalatest"            % "3.2.12"
    )
  )
