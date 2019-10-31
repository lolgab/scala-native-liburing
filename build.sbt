import scala.sys.process._

scalaVersion := "2.11.12"

enablePlugins(ScalaNativePlugin)

lazy val make = taskKey[Unit]("Building c files")
make := {
  "make".!
}
nativeLinkingOptions := Seq("-L" ++ baseDirectory.value.getAbsolutePath() ++ "/target")
Compile / compile := (Compile / compile dependsOn make).value