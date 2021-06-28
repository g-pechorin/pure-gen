
/// ====
// monorepo config block

import sbt.Def
import java.io.File

val hgRoot: File = {
	var root = file("").getAbsoluteFile.getParentFile

	while (!(root / "sbt.bin/scala.conf").exists())
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
// end of monorepo config block

organization := "com.peterlavalle"
scalaVersion := conf("scala.version")
scalacOptions ++= conf("scala.options").split("/").toSeq

resolvers += Classpaths.typesafeReleases
resolvers += Resolver.mavenCentral
resolvers += Resolver.jcenterRepo
resolvers += "jitpack" at "https://jitpack.io"

// end of standard stuff
/// ---

name := "pureGenerator"

libraryDependencies += "com.github.g-pechorin" % "peterlavalle.sbt" % "26729b2"

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

libraryDependencies ++=
	Seq(
		"com.lihaoyi" %% "fastparse" % "2.2.2",
		"org.scalatest" %% "scalatest" % conf("scala.test") % Test,
		// "org.scala-sbt" %% "io" % conf("sbt.version"),
	)
