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

lazy val captainModelLibraries = Seq(scalaTest)
lazy val sailorLibraries = Seq(scalaTest, akkaStream, akkaStreamTestKit, akkaCluster)

lazy val `captain-model` = (project in file("captain-model"))
  .settings(
    commonSettings,
    libraryDependencies ++= captainModelLibraries
  )

lazy val sailor = (project in file("sailor"))
  .settings(
    commonSettings,
    libraryDependencies ++= sailorLibraries
  )
  .dependsOn(`captain-model`)

lazy val captain = (project in file("."))
  .settings(
    name := "captain",
    commonSettings
  )
  .aggregate(sailor)