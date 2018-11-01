lazy val commonSettings = Seq(
  name := "eShop",
  version := "1.0",
  scalaVersion := "2.12.7",
  fork in Test := true
)

lazy val akkaVersion = "2.5.17"
lazy val akkaHttpVersion = "10.1.5"
lazy val levelDbVersion = "0.10"
lazy val levelDbJniAllVersion = "1.8"
lazy val scalaTestVersion = "3.0.5"
lazy val gatlingVersion = "3.0.0"
lazy val slickVersion = "3.2.3"
lazy val slf4jNopVersion = "1.7.25"
lazy val postgresVersion = "9.4-1206-jdbc42"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "eShop"
  ).aggregate(eShopCore, eShopRestApi, eShopShared, eShopPerformanceTests, eShopDatabase)
   .dependsOn(eShopCore, eShopRestApi, eShopShared, eShopPerformanceTests, eShopDatabase)

lazy val eShopCore = (project in file("eShop-core")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "org.iq80.leveldb" % "leveldb" % levelDbVersion,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % levelDbJniAllVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  ).dependsOn(eShopDatabase, eShopShared)

lazy val eShopRestApi = (project in file("eShop-rest-api")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-rest-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  ).dependsOn(eShopCore, eShopShared)

lazy val eShopShared = (project in file("eShop-shared")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-shared",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    )
  )

lazy val eShopPerformanceTests = (project in file("eShop-performance-tests")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-performance-tests",
    libraryDependencies ++= Seq(
      "io.gatling"            % "gatling-test-framework"    % gatlingVersion,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion
    )
  ).enablePlugins(GatlingPlugin)

lazy val eShopDatabase = (project in file("eShop-database")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-database",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "org.slf4j" % "slf4j-nop" % slf4jNopVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.postgresql" % "postgresql" % postgresVersion,
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  ).dependsOn(eShopShared)

libraryDependencies ~= { _.map(_
  .exclude("ch.qos.logback", "logback-classic*"))
}
