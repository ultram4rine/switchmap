lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := """ru.sgu""",
    name := """switchmap""",
    version := "2.0-SNAPSHOT",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      cacheApi,
      guice,
      jdbc
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play"      %% "play-slick"         % "5.0.0",
      "com.pauldijou"          %% "jwt-core"           % "4.3.0",
      "com.pauldijou"          %% "jwt-play"           % "4.3.0",
      "com.unboundid"           % "unboundid-ldapsdk"  % "5.1.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
