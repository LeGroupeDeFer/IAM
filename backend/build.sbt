val airFrameHttpVersion = "20.10.0"
val slickVersion = "3.3.3"
val slf4jVersion = "1.6.4"
val slickHikaricpVersion = "3.3.3"
val scalatestVersion = "3.2.2"
val mysqlConnectorVersion = "8.0.19"
val bcryptVersion = "0.4"
val jwtVersion = "4.2.0"
val scoptVersion = "4.0.0-RC2"
val commonCodecVersion = "1.9"
val flywayVersion = "7.2.1"
val eddsaVersion = "0.3.0"
val iamScalaVersion = sys.env.get("SCALA_VERSION").getOrElse("2.12.7")

enablePlugins(JavaAppPackaging)

lazy val root = (project in file("."))
  .settings(
    organization := "be.unamur.infom453",
    name := "iam",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := iamScalaVersion,
    libraryDependencies ++= Seq(
      "org.wvlet.airframe" %% "airframe-http-finagle" % airFrameHttpVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "org.slf4j" % "slf4j-nop" % slf4jVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickHikaricpVersion,
      "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
      "org.mindrot" % "jbcrypt" % bcryptVersion,
      "com.pauldijou" %% "jwt-circe" % jwtVersion,
      "commons-codec" % "commons-codec" % commonCodecVersion,
      "com.github.scopt" %% "scopt" % scoptVersion,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "net.i2p.crypto" % "eddsa" % eddsaVersion
    )
  )