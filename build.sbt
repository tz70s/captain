lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6"
)

// Test dependencies
val scalaTestVersion = "3.0.5"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test

// Akka dependencies
val akkaVersion = "2.5.15"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaActorTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test

val pureConfigVersion = "0.9.2"
val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

// Play
val playVersion = "2.6.7"
val playJson = "com.typesafe.play" %% "play-json" % playVersion

lazy val captainModelDependencies = Seq(scalaTest, playJson)
lazy val captainToolDependencies = Seq(scalaTest, pureConfig)
lazy val sailorDependencies = Seq(scalaTest, akkaStream, akkaStreamTestKit, akkaCluster, akkaActor, akkaActorTestKit)

lazy val `captain-model` = (project in file("captain-model"))
  .settings(
    commonSettings,
    libraryDependencies ++= captainModelDependencies
  )

lazy val `captain-tool` = (project in file("captain-tool"))
  .settings(
    commonSettings,
    libraryDependencies ++= captainToolDependencies
  )

lazy val `captain-log` = (project in file("captain-log"))
  .settings(
    commonSettings,
    // currently depends on akka event logging, may be better drop this to reduce code size...
    libraryDependencies += akkaActor
  )

val sailorJVMOpts = Seq("-Xms32M", "-Xmx256M")

lazy val sailor = (project in file("sailor"))
  .settings(
    commonSettings,
    libraryDependencies ++= sailorDependencies,
    run / fork := true,
    run / javaOptions ++= sailorJVMOpts
  )
  .dependsOn(`captain-model`, `captain-log`)

lazy val captain = (project in file("."))
  .settings(
    name := "captain",
    commonSettings
  )
  .aggregate(sailor, `captain-model`, `captain-tool`)