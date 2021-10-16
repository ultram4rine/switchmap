val Http4sVersion = "0.23.6"
val DoobieVersion = "0.13.4"
val PureConfigVersion = "0.17.0"

lazy val root = (project in file("."))
  .settings(
    organization := "ru.sgu",
    name := "switchmap",
    version := "2.0.0-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                    % "1.0.12",
      "dev.zio"               %% "zio-interop-cats"       % "2.1.4.0",
      "org.http4s"            %% "http4s-blaze-server"    % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"    % Http4sVersion,
      "org.http4s"            %% "http4s-circe"           % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"             % Http4sVersion,
      "io.circe"              %% "circe-generic"          % "0.14.1",
      "org.specs2"            %% "specs2-core"            % "4.13.0" % "test",
      "ch.qos.logback"         % "logback-classic"        % "1.2.6",
      "org.tpolecat"          %% "doobie-core"            % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"        % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"          % DoobieVersion,
      "org.flywaydb"           % "flyway-core"            % "8.0.1",
      "org.postgresql"         % "postgresql"             % "42.2.24",
      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "com.pauldijou"         %% "jwt-circe"              % "5.0.0",
      "com.unboundid"          % "unboundid-ldapsdk"      % "6.0.2",
      "org.snmp4j"             % "snmp4j"                 % "3.5.1"
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
