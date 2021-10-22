val Versions =
  new {
    val caliban = "1.2.1"
    val http4s = "0.23.5"
  }

ThisBuild / scalaVersion := "3.1.0"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

val commonSettings: Seq[Setting[_]] = Seq(
  scalacOptions -= "-Xfatal-warnings",
  libraryDependencies ++= Seq(
    "com.kubukoz" %% "debug-utils" % "1.1.3",
    "org.typelevel" %% "log4cats-slf4j" % "2.1.1",
    "org.typelevel" %% "cats-effect" % "3.2.9",
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
    compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.10" cross CrossVersion.full),
  ),
)

val core = project.settings(
  commonSettings
)

val root = project
  .in(file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % Versions.caliban,
      "com.github.ghostdogpr" %% "caliban-http4s" % Versions.caliban,
      "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
      "ch.qos.logback" % "logback-classic" % "1.2.6",
    ),
    publish := {},
    publish / skip := true,
  )
  .dependsOn(core)
  .aggregate(core)
