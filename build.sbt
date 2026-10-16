import sbtcrossproject.CrossPlugin.autoImport.crossProject

ThisBuild / scalaVersion := "3.3.7"
ThisBuild / crossScalaVersions := Seq("3.3.7")
ThisBuild / organization := "com.slamdata"
ThisBuild / homepage := Some(url("https://github.com/unktomi/matryoshka"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

val scalazVersion = "7.2.36"
val monocleVersion = "1.6.3"
val specs2Version = "4.23.0"

lazy val standardSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Ykind-projector", "-language:implicitConversions"),
  libraryDependencies ++= Seq(
    ("org.scalaz" %%% "scalaz-core" % scalazVersion).cross(CrossVersion.for3Use2_13),
    // Keep Monocle and Scalaz on the same Scala 2.13 ABI via Scala 3 interoperability.
    // Their Scala 2/3 idInstance intersection return types erase differently.
    ("com.github.julien-truffaut" %%% "monocle-core" % monocleVersion)
      .cross(CrossVersion.for3Use2_13).excludeAll(ExclusionRule("org.scalaz"))),
  Test / logBuffered := false
)

lazy val root = project.in(file("."))
  .settings(name := "matryoshka", publish / skip := true)
  .aggregate(coreJVM, coreJS, scalacheckJVM, scalacheckJS, testsJVM, testsJS)

lazy val core = crossProject(JVMPlatform, JSPlatform).in(file("core"))
  .settings(standardSettings)
  .settings(name := "matryoshka-core")

lazy val scalacheck = crossProject(JVMPlatform, JSPlatform).in(file("scalacheck"))
  .dependsOn(core)
  .settings(standardSettings)
  .settings(
    name := "matryoshka-scalacheck",
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.18.1",
      ("org.scalaz" %%% "scalaz-scalacheck-binding" % (scalazVersion + "-scalacheck-1.15"))
        .cross(CrossVersion.for3Use2_13).excludeAll(ExclusionRule("org.scalacheck"))))

lazy val tests = crossProject(JVMPlatform, JSPlatform).in(file("tests"))
  .dependsOn(core, scalacheck)
  .settings(standardSettings)
  .settings(
    name := "matryoshka-tests",
    publish / skip := true,
    libraryDependencies ++= Seq(
      ("com.github.julien-truffaut" %%% "monocle-law" % monocleVersion % Test)
        .cross(CrossVersion.for3Use2_13)
        .excludeAll(ExclusionRule("org.scalaz"), ExclusionRule("org.scalacheck")),
      ("org.typelevel" %%% "discipline-specs2" % "1.5.0" % Test)
        .excludeAll(ExclusionRule("org.typelevel", "discipline-core_3"), ExclusionRule("org.typelevel", "discipline-core_sjs1_3")),
      // Monocle's Scala 2.13 law classes call the Scala 2 trait initializer.
      ("org.typelevel" %%% "discipline-core" % "1.7.0" % Test).cross(CrossVersion.for3Use2_13)
        .excludeAll(ExclusionRule("org.scalacheck")),
      "org.specs2" %%% "specs2-scalacheck" % specs2Version % Test,
      "org.specs2" %%% "specs2-core" % specs2Version % Test))

lazy val docs = project.in(file("docs"))
  .dependsOn(coreJVM)
  .enablePlugins(MdocPlugin)
  .settings(standardSettings)
  .settings(publish / skip := true, mdocIn := file("docs/src/main/mdoc"), mdocVariables := Map("VERSION" -> version.value))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val scalacheckJVM = scalacheck.jvm
lazy val scalacheckJS = scalacheck.js
// The inherited McCarthy-91 example constructs its first Partial eagerly.
// Give its documented, stack-dependent lower-bound test the same explicit
// stack budget on both runtimes; this is not a stack-safety test.
lazy val testsJVM = tests.jvm.settings(
  Test / fork := true,
  Test / javaOptions += "-Xss4m")
lazy val testsJS = tests.js.settings(
  Test / jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(
    org.scalajs.jsenv.nodejs.NodeJSEnv.Config().withArgs(List("--stack-size=4096"))))
