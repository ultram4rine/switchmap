val ScalatraVersion = "2.7.0"
val JettyVersion = "9.4.27.v20200227"

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl, ScalatraPlugin)
  .settings()

organization := "ru.sgu"
name := "switchmap"
version := "2.0.0-SNAPSHOT"

scalaVersion := "2.13.1"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra"       %% "scalatra"           % ScalatraVersion,
  "org.scalatra"       %% "scalatra-auth"      % ScalatraVersion,
  "org.scalatra"       %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback"     % "logback-classic"     % "1.2.3" % "runtime",
  "org.eclipse.jetty"  % "jetty-webapp"        % JettyVersion % "provided",
  "javax.servlet"      % "javax.servlet-api"   % "4.0.1" % "provided",
  "com.typesafe.slick" %% "slick"              % "3.3.2",
  "org.postgresql"     % "postgresql"          % "42.2.11",
  "com.mchange"        % "c3p0"                % "0.9.5.5",
  "com.comcast"        %% "ip4s-core"          % "1.3.0"
)
