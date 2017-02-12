lazy val commonSettings = Seq(
  name := "eShop",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"
lazy val levelDbVersion = "0.7"
lazy val levelDbJniAllVersion = "1.8"
lazy val scalaTestVersion = "3.0.1"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "eShop"
  ).aggregate(eShopCore, eShopRestApi, eShopShared).dependsOn(eShopCore, eShopRestApi, eShopShared)

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
  )

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
