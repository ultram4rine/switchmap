import NativePackagerHelper._

val Http4sVersion = "0.23.11"
val TapirVersion = "0.20.0-M7"
val DoobieVersion = "1.0.0-RC2"
val PureConfigVersion = "0.17.1"

lazy val root = (project in file("."))
  .settings(
    organization := "ru.sgu",
    name := "switchmap",
    version := "2.0.0",
    scalaVersion := "2.13.8",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "dev.zio"    %% "zio"                 % "1.0.13",
      "dev.zio"    %% "zio-interop-cats"    % "3.2.9.1",
      "dev.zio"    %% "zio-logging-slf4j"   % "0.5.14",
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion,
      "io.circe"                    %% "circe-generic"           % "0.14.1",
      "org.tpolecat"           %% "doobie-core"            % DoobieVersion,
      "org.tpolecat"           %% "doobie-postgres"        % DoobieVersion,
      "org.tpolecat"           %% "doobie-hikari"          % DoobieVersion,
      "org.polyvariant"        %% "doobie-quill"           % "0.0.5",
      "org.flywaydb"            % "flyway-core"            % "8.5.4",
      "org.postgresql"          % "postgresql"             % "42.3.3",
      "com.github.jwt-scala"   %% "jwt-circe"              % "9.0.4",
      "com.unboundid"           % "unboundid-ldapsdk"      % "6.0.4",
      "com.github.pureconfig"  %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig"  %% "pureconfig-cats-effect" % PureConfigVersion,
      "com.github.pureconfig"  %% "pureconfig-http4s"      % PureConfigVersion,
      "com.github.pureconfig"  %% "pureconfig-ip4s"        % PureConfigVersion,
      "com.github.seancfoley"   % "ipaddress"              % "5.3.3",
      "org.snmp4j"              % "snmp4j"                 % "3.6.5",
      "com.softwaremill.diffx" %% "diffx-core"             % "0.7.0",
      "ch.qos.logback"          % "logback-classic"        % "1.2.11",
      "io.grpc"                 % "grpc-netty"             % "1.45.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    ),
    dependencyOverrides ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.32" // doobie-hikari@1.0.0-RC1 -> HikariCP@4.0.3 -> slf4j-api@2.0.0-alpha.1
    ),
    addCompilerPlugin(
      "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
  .enablePlugins(JavaServerAppPackaging, RpmPlugin, SystemdPlugin)
  .settings(
    Linux / packageName := "switchmap",
    Linux / maintainer := "SGU <sts@sgu.ru>",
    Linux / packageSummary := "Interactive map of SSU switches",
    Linux / packageDescription := "Interactive map of SSU switches",
    rpmRelease := "14",
    rpmVendor := "SGU",
    rpmUrl := Some("https://git.sgu.ru/ultramarine/switchmap"),
    rpmLicense := Some("Apache-2.0"),
    Universal / mappings ++= directory("plans"),
    bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
  "-Wunused:imports"
)
