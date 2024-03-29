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
val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
val akkaDistributedData = "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaActorTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
val akkaMultiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion // multi-node plugin should not be marked as test usage here.

// Alpakka
val alpakkaVersion = "0.20"
val alpakkaMqtt = "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % alpakkaVersion

val pureConfigVersion = "0.9.2"
val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

// Play
val playVersion = "2.6.7"
val playJson = "com.typesafe.play" %% "play-json" % playVersion

lazy val captainModelDependencies = Seq(scalaTest, playJson)
lazy val captainToolDependencies = Seq(scalaTest, pureConfig)
lazy val sailorDependencies = Seq(scalaTest, akkaStream, akkaStreamTestKit, akkaCluster, akkaActor, akkaActorTestKit, alpakkaMqtt)
lazy val captainFrameworkDependencies = Seq(scalaTest, akkaStream, akkaStreamTestKit, akkaCluster, akkaClusterTools, akkaClusterSharding, akkaActor, akkaActorTestKit, akkaMultiNodeTestKit, akkaDistributedData)

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

val sailorJVMOpts = Seq("-Xms32M", "-Xmx256M")

lazy val `captain-sailor` = (project in file("captain-sailor"))
  .settings(
    commonSettings,
    libraryDependencies ++= sailorDependencies,
    run / fork := true,
    run / javaOptions ++= sailorJVMOpts
  )
  .dependsOn(`captain-framework`)
  .enablePlugins(JavaAppPackaging, JavaServerAppPackaging)

lazy val `captain-framework` = (project in file("captain-framework"))
  .settings(
    commonSettings,
    libraryDependencies ++= captainFrameworkDependencies,
    jvmOptions in MultiJvm := Seq("-Xmx256M"),
    coverageEnabled := true
  )
  .dependsOn(`captain-model`, `captain-tool`)
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)

lazy val `example` = (project in file("example"))
  .settings(
    commonSettings
  )
  .dependsOn(`captain-framework`)

lazy val captain = (project in file("."))
  .settings(
    name := "captain",
    commonSettings
  )
  .aggregate(`captain-sailor`, `captain-model`, `captain-tool`, `captain-framework`, `example`)
