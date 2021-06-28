
/// ====
// monorepo config block

import sbt.Def
import java.io.File

val hgRoot: File = {
	var root = file("").getAbsoluteFile

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

import java.io.File

// declare settings
val idlRoots = settingKey[Seq[File]]("folders to scan for .pidl in")
val idlTargetScala = settingKey[File]("where pidl should put .scala")
val idlTargetScript = settingKey[File]("where pidl should put .purs and .js")

// configure the settings
idlRoots := {
	Seq((Compile / sourceDirectory).value / "pidl")
}
idlTargetScala := {
	(Compile / sourceManaged).value / "pidl-scala"
}
idlTargetScript := {
	target.value / "spago/gen"
}

// declare the two tasks
val idlGenerateScala: TaskKey[List[File]] = taskKey[List[File]]("generate some scala")
val idlGenerateScript: TaskKey[List[File]] = taskKey[List[File]]("generate some scala")

// define the tasks
// scala task
Compile / sourceGenerators += idlGenerateScala
idlGenerateScala := {
	val pak = "S3" // organization.value + "." + name.value
	val src = idlRoots.value
	val out = idlTargetScala.value
	import peterlavalle.puregen._

	S3.Scala(pak, src, out)
}
// purescript generation tasks
Compile / resourceGenerators += idlGenerateScript
idlGenerateScript := {
	val pak = "S3" // organization.value + "." + name.value
	val src = idlRoots.value
	val out = idlTargetScript.value
	import peterlavalle.puregen._
	S3.Script(pak, src, out)
}
