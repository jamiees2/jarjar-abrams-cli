ThisBuild / scalaVersion := "2.12.11"
ThisBuild / version := "0.1.2"
ThisBuild / organization := "io.github.jamiees2"
ThisBuild / organizationName := "jarjar-abrams-cli"
ThisBuild / description := "A CLI interface onto jarjar-abrams for shading"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/jamiees2/jarjar-abrams-cli"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/jamiees2/jarjar-abrams-cli"),
    "scm:git@github.com:jamiees2/jarjar-abrams-cli.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "jamiees2",
    name = "James Elías Sigurðarson",
    email = "jamiees2@gmail.com",
    url = url("https://github.com/jamiees2/")
  )
)

scalacOptions ++= Seq(
  "-feature", // Warn if features are used that should be imported explicitly
  "-deprecation", // Warn about deprecations.
  "-unchecked", // Catch unchecked types due to type erasure.
  "-encoding",
  "UTF-8", // Explicit encoding to ensure uniformity across platforms.
  "-Ywarn-value-discard", // Warn when a value is returned from a Unit function.
  "-Ywarn-dead-code", // Warn about unreachable code
  "-Ywarn-unused", // Warn about unused values
  "-Xlint", // Enable linting
  "-Xfuture", // this should also disable adapted args.
  "-Yno-adapted-args", // This is a terrible feature...
  "-Ywarn-numeric-widen", // Don't accidentally change numeric types
  "-Yrangepos",
  "-Ywarn-unused:-explicits", // Disable warning about explicit values
  "-Ywarn-unused:-implicits", // Disable warning about implicits
  "-Ywarn-unused:-patvars", // Disable warning about pattern matched variables
)

lazy val root = (project in file("."))
  .settings(
    name := "jarjar-abrams-cli",
    libraryDependencies ++= Seq(
      "com.eed3si9n.jarjarabrams" %% "jarjar-abrams-core" % "0.3.0",
      "org.rogach" %% "scallop" % "3.2.0",
      "org.json4s" %% "json4s-jackson" % "3.4.2",
      "com.lihaoyi" %% "os-lib" % "0.7.3",
    )
  )

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

import sbtassembly.AssemblyPlugin.defaultUniversalScript

assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(prependShellScript = Some(defaultUniversalScript(shebang = true)))

assemblyJarName in assembly := s"${name.value}-${version.value}"
