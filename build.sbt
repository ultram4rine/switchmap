val Http4sVersion = "0.21.7"
val CirceVersion = "0.13.0"
val Specs2Version = "4.10.3"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.9.2"
val PureConfigVersion = "0.14.0"

lazy val root = (project in file("."))
  .settings(
    organization := "ru.sgu",
    name := "switchmap",
    version := "2.0.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"    % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"    % Http4sVersion,
      "org.http4s"            %% "http4s-circe"           % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"             % Http4sVersion,
      "io.circe"              %% "circe-generic"          % CirceVersion,
      "org.specs2"            %% "specs2-core"            % Specs2Version % "test",
      "ch.qos.logback"         % "logback-classic"        % LogbackVersion,
      "org.tpolecat"          %% "doobie-core"            % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"        % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"          % DoobieVersion,
      "org.flywaydb"           % "flyway-core"            % "6.5.7",
      "org.postgresql"         % "postgresql"             % "42.2.16",
      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "com.pauldijou"         %% "jwt-circe"              % "4.3.0",
      "com.unboundid"          % "unboundid-ldapsdk"      % "5.1.1",
      "org.snmp4j"             % "snmp4j"                 % "3.4.3"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
