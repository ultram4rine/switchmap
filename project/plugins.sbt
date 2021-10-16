//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.2")
addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"           % "0.9.19")
addSbtPlugin("io.spray"       % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.eed3si9n"   % "sbt-assembly"           % "0.14.10")
addSbtPlugin("com.thesamet"   % "sbt-protoc"             % "1.0.4")

libraryDependencies +=
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.5.1"
