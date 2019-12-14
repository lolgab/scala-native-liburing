import scala.sys.process._

val commonSettings = Seq(
  scalaVersion := "2.11.12"
)

lazy val uring = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    nativeLinkingOptions := Seq(
      "-L" ++ baseDirectory.value.getAbsolutePath() ++ "/target"
    ),
    Compile / compile := (Compile / compile dependsOn make).value,
    libraryDependencies += "com.github.lolgab" %%% "scala-native-http" % "0.1.0-SNAPSHOT"
  )
  .enablePlugins(ScalaNativePlugin)

lazy val make = taskKey[Unit]("Building c files")
make := {
  "make".!
}

lazy val httpExample = project
  .settings(
    )
  .dependsOn(uring)
