lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := """ru.sgu""",
    name := """switchmap""",
    version := "2.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    resolvers += Classpaths.typesafeReleases,
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= Seq(
      caffeine,
      guice,
      jdbc,
      ws
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play"      %% "play-slick"               % "5.0.0",
      "org.postgresql"          % "postgresql"               % "42.2.14",
      "com.pauldijou"          %% "jwt-play-json"            % "4.3.0",
      "com.unboundid"           % "unboundid-ldapsdk"        % "5.1.0",
      "org.snmp4j"              % "snmp4j"                   % "3.4.1",
      "net.codingwell"         %% "scala-guice"              % "4.2.7",
      "net.logstash.logback"    % "logstash-logback-encoder" % "6.3",
      "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
