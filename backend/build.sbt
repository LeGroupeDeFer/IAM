val airFrameHttpVersion   = "20.10.0"
val slickVersion          = "3.3.3"
val slf4jVersion          = "1.6.4"
val slickHikaricpVersion  = "3.3.3"
val scalatestVersion      = "3.0.5"
val mysqlConnectorVersion = "6.0.6"
val bcryptVersion         = "0.4"
val jwtVersion            = "4.2.0"

lazy val root = (project in file("."))
  .settings(
    organization := "be.unamur.infom453",
    name := "iam",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "org.wvlet.airframe" %% "airframe-http-finagle" % (airFrameHttpVersion),
      "org.scalatest"      %% "scalatest"             % scalatestVersion % "test",
      "com.typesafe.slick" %% "slick"                 % slickVersion,
      "org.slf4j"           % "slf4j-nop"             % slf4jVersion,
      "com.typesafe.slick" %% "slick-hikaricp"        % slickHikaricpVersion,
      "mysql"               % "mysql-connector-java"  % mysqlConnectorVersion,
      "org.mindrot"         % "jbcrypt"               % bcryptVersion,
      "com.pauldijou"      %% "jwt-circe"             % jwtVersion
    )
  )