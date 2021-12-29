name               := "akka-logback"
ThisBuild / scalaVersion       := "2.13.6"
ThisBuild / crossScalaVersions := Seq(scalaVersion.value, "2.12.14")

Test / fork := true
Test / testGrouping := (Test / definedTests).value.map { suite =>
  import Tests._
  Group(suite.name, Seq(suite), SubProcess(ForkOptions()))
}

ThisBuild / tlBaseVersion := "0.1"
enablePlugins(TypelevelCiReleasePlugin)
ThisBuild / tlCiReleaseSnapshots := true
ThisBuild / tlCiReleaseBranches := Seq("series/sbt-typelevel")

ThisBuild / tlHashSnapshots := false

val AkkaVersion = "2.6.16"
libraryDependencies := Seq(
  "ch.qos.logback"     % "logback-classic"          % "1.2.5",
  "com.typesafe.akka" %% "akka-actor"               % AkkaVersion,
  "org.scalatest"     %% "scalatest"                % "3.2.9"     % Test,
  "com.typesafe.akka" %% "akka-actor-typed"         % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"             % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-slf4j"               % AkkaVersion % Test
)

organization     := "com.armanbilge"
organizationName := "Arman Bilge"
startYear        := Some(2021)
licenses         := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage         := Some(url("https://github.com/armanbilge/akka-logback"))
developers := List(
  Developer(
    id = "armanbilge",
    name = "Arman Bilge",
    email = "arman@armanbilge.com",
    url = url("https://github.com/armanbilge")
  )
)
