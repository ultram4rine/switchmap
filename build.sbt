val SilhouetteVersion = "7.0.0"
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := """ru.sgu""",
    name := """switchmap""",
    version := "2.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    resolvers ++= Seq(Classpaths.typesafeReleases, Resolver.jcenterRepo),
    libraryDependencies ++= Seq(
      caffeine,
      guice,
      jdbc,
      ws
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play"      %% "play-slick"                      % "5.0.0",
      "org.postgresql"          % "postgresql"                      % "42.2.14",
      "com.pauldijou"          %% "jwt-play-json"                   % "4.3.0",
      "com.unboundid"           % "unboundid-ldapsdk"               % "5.1.0",
      "org.snmp4j"              % "snmp4j"                          % "3.4.2",
      "com.mohiva"             %% "play-silhouette"                 % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-password-bcrypt" % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-persistence"     % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-crypto-jca"      % SilhouetteVersion,
      "com.mohiva"             %% "play-silhouette-totp"            % SilhouetteVersion,
      "net.codingwell"         %% "scala-guice"                     % "4.2.11",
      "com.iheart"             %% "ficus"                           % "1.4.7",
      "net.logstash.logback"    % "logstash-logback-encoder"        % "6.3",
      "org.scalatestplus.play" %% "scalatestplus-play"              % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
