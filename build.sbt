val SilhouetteVersion = "7.0.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := """ru.sgu""",
    name := """switchmap""",
    version := "2.0-SNAPSHOT",
    scalaVersion := "2.13.2",
    resolvers += Classpaths.typesafeReleases,
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= Seq(
      cacheApi,
      guice,
      jdbc
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play"      %% "play-slick"                      % "5.0.0",
      "org.postgresql"          % "postgresql"                      % "42.2.14",
      "com.mohiva"             %% "play-silhouette"                 % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-password-bcrypt" % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-persistence"     % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-crypto-jca"      % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-totp"            % SilhouetteVersion,
      "com.unboundid"           % "unboundid-ldapsdk"               % "5.1.0",
      "net.logstash.logback"    % "logstash-logback-encoder"        % "6.4",
      "org.scalatestplus.play" %% "scalatestplus-play"              % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
