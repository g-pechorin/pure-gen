import java.io.File

lazy val all =
	Seq(Compile, Test).flatMap {
		from =>
			Seq(
				// ensure that we're reading resources from the scala source paths
				(unmanagedResourceDirectories in from) += ((scalaSource in from).value),
			)
	}
lazy val root =
	(project in file("."))
		.settings(all: _ *)

scalaVersion := conf("scala.version")
name := "pureGenerator"
resolvers += Classpaths.typesafeReleases
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

dependsOn(
	RootProject(hgRoot / "peterlavalle-minibase.sbt")
)

libraryDependencies ++=
	Seq(
		"com.lihaoyi" %% "fastparse" % "2.2.2",
		"org.scalatest" %% "scalatest" % conf("scala.test") % Test,
		// "org.scala-sbt" %% "io" % conf("sbt.version"),
	)


