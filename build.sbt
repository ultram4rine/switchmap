val ScalatraVersion = "2.7.0"
val SlickVersion = "3.3.2"
val JettyVersion = "9.4.27.v20200227"

lazy val root = (project in file("."))
  .enablePlugins(ScalatraPlugin)
  .settings()

organization := "ru.sgu"
name := "switchmap"
version := "2.0.0-SNAPSHOT"

scalaVersion := "2.13.2"

containerPort in Jetty := 49147

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra"       %% "scalatra"           % ScalatraVersion,
  "org.scalatra"       %% "scalatra-auth"      % ScalatraVersion,
  "org.scalatra"       %% "scalatra-scalatest" % ScalatraVersion % "test",
  "org.scalatra"       %% "scalatra-json"      % ScalatraVersion,
  "org.json4s"         %% "json4s-jackson"     % "3.6.8",
  "com.pauldijou"      %% "jwt-json4s-jackson" % "4.3.0",
  "com.typesafe.slick" %% "slick"              % SlickVersion,
  "com.typesafe.slick" %% "slick-hikaricp"     % SlickVersion,
  "org.postgresql"     % "postgresql"          % "42.2.12",
  "org.eclipse.jetty"  % "jetty-webapp"        % JettyVersion % "provided",
  "javax.servlet"      % "javax.servlet-api"   % "4.0.1" % "provided",
  "ch.qos.logback"     % "logback-classic"     % "1.2.3" % "runtime",
  "org.slf4j"          % "slf4j-nop"           % "1.7.30"
)
