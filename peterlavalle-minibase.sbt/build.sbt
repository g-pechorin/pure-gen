import java.io.File

val hgRoot: File = {
	var root = file("").getAbsoluteFile.getParentFile

	while (!(root / ".hg").exists())
		root = root.getAbsoluteFile.getParentFile.getAbsoluteFile
	root
}

def conf: String => String = {
	import com.typesafe.config.ConfigFactory

	(key: String) =>
		ConfigFactory.parseFile(
			hgRoot / "sbt.bin/scala.conf"
		).getString(key)
}

organization := "com.peterlavalle"
scalaVersion := conf("scala.version")
scalacOptions ++= conf("scala.options").split("/").toSeq
testOptions in Test ++= Seq(
	Tests.Argument("-oD", "-u", "target/test-reports")
)

name := "minibase"

libraryDependencies ++=
	Seq(
		// "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
		"org.scalatest" %% "scalatest" % conf("scala.test") % Test,
		"org.easymock" % "easymock" % "4.0.2" % Test,
	)

