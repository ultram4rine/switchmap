import NativePackagerHelper._

val Http4sVersion = "0.23.6"
val RhoVersion = "0.23.0-RC1"
val DoobieVersion = "1.0.0-RC1"
val PureConfigVersion = "0.17.1"

lazy val root = (project in file("."))
  .settings(
    organization := "ru.sgu",
    name := "switchmap",
    version := "2.0.0",
    scalaVersion := "3.0.0",
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "dev.zio"     %% "zio"                 % "1.0.12",
      "dev.zio"     %% "zio-interop-cats"    % "3.2.9.0",
      "dev.zio"     %% "zio-logging-slf4j"   % "0.5.14",
      "org.http4s"  %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"  %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"  %% "http4s-circe"        % Http4sVersion,
      "org.http4s"  %% "http4s-dsl"          % Http4sVersion,
      ("org.http4s" %% "rho-swagger"         % RhoVersion)
        .cross(CrossVersion.for3Use2_13),
      ("org.http4s" %% "rho-swagger-ui" % RhoVersion)
        .cross(CrossVersion.for3Use2_13),
      "io.circe"      %% "circe-generic"   % "0.14.1",
      "org.tpolecat"  %% "doobie-core"     % DoobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % DoobieVersion,
      ("org.tpolecat" %% "doobie-quill"    % DoobieVersion)
        .cross(CrossVersion.for3Use2_13),
      "org.tpolecat"           %% "doobie-hikari"     % DoobieVersion,
      "org.flywaydb"            % "flyway-core"       % "8.2.0",
      "org.postgresql"          % "postgresql"        % "42.3.1",
      "com.github.jwt-scala"   %% "jwt-circe"         % "9.0.2",
      "com.unboundid"           % "unboundid-ldapsdk" % "6.0.3",
      ("com.github.pureconfig" %% "pureconfig"        % PureConfigVersion)
        .cross(CrossVersion.for3Use2_13),
      ("com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion)
        .cross(CrossVersion.for3Use2_13),
      ("com.github.pureconfig" %% "pureconfig-http4s" % PureConfigVersion)
        .cross(CrossVersion.for3Use2_13),
      ("com.github.pureconfig" %% "pureconfig-ip4s" % PureConfigVersion)
        .cross(CrossVersion.for3Use2_13),
      "com.github.seancfoley" % "ipaddress"       % "5.3.3",
      "org.snmp4j"            % "snmp4j"          % "3.6.3",
      "ch.qos.logback"        % "logback-classic" % "1.2.7",
      "io.grpc"               % "grpc-netty"      % "1.42.1",
      ("com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion)
        .cross(CrossVersion.for3Use2_13)
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
    rpmRelease := "6",
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
  "-Xfatal-warnings"
)
