lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := """ru.sgu""",
    name := """switchmap""",
    version := "2.0-SNAPSHOT",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play"      %% "play-slick"         % "5.0.0",
      "com.unboundid"          % "unboundid-ldapsdk"   % "2.3.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
