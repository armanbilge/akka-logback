name := "akka-logback"
scalaVersion := "2.13.6"
crossScalaVersions := Seq(scalaVersion.value, "2.12.14")
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-encoding",
  "UTF-8",
  "-Ywarn-unused:imports",
  "-target:jvm-1.8"
)
scalafmtOnCompile := true
Compile / scalafmt := {
  val _ = (Compile / scalafmtSbt).value
  (Compile / scalafmt).value
}

Test / fork := true
Test / testGrouping := (Test / definedTests).value.map { suite =>
  import Tests._
  Group(suite.name, Seq(suite), SubProcess(ForkOptions()))
}

addCommandAlias("codeStyleCheck", "headerCheck; scalafmtCheckAll; scalafmtSbtCheck")

enablePlugins(GitVersioning)
enablePlugins(AutomateHeaderPlugin)

val AkkaVersion = "2.6.15"
libraryDependencies := Seq(
  "ch.qos.logback"     % "logback-classic"          % "1.2.5",
  "com.typesafe.akka" %% "akka-actor"               % AkkaVersion,
  "org.scalatest"     %% "scalatest"                % "3.2.9"     % Test,
  "com.typesafe.akka" %% "akka-actor-typed"         % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"             % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-slf4j"               % AkkaVersion % Test
)

organization := "com.armanbilge"
organizationName := "Arman Bilge"
startYear := Some(2021)
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/armanbilge/akka-logback"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/armanbilge/akka-logback"),
    "git@github.com:armanbilge/akka-logback.git"
  )
)
developers := List(
  Developer(
    id = "armanbilge",
    name = "Arman Bilge",
    email = "arman@armanbilge.com",
    url = url("https://github.com/armanbilge")
  )
)
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
