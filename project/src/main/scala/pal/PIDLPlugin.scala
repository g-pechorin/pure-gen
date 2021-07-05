package pal

import sbt.Keys._
import sbt.{AutoPlugin, _}


object PIDLPlugin extends AutoPlugin {
	// override def trigger = allRequirements

	object autoImport {

		// declare settings
		val idlRoots = settingKey[Seq[File]]("folders to scan for .pidl in")
		val idlTargetScala = settingKey[File]("where pidl should put .scala")
		val idlTargetScript = settingKey[File]("where pidl should put .purs and .js")

		// declare the two tasks
		val idlGenerateScala: TaskKey[List[File]] = taskKey[List[File]]("generate some scala")
		val idlGenerateScript: TaskKey[List[File]] = taskKey[List[File]]("generate some scala")

	}

	import autoImport._

	override lazy val projectSettings: Seq[Setting[_]] = Seq(

		// configure the settings
		idlRoots := {
			Seq((Compile / sourceDirectory).value / "pidl")
		},
		idlTargetScala := {
			(Compile / sourceManaged).value / "pidl-scala"
		},
		idlTargetScript := {
			target.value / "spago/gen"
		},

		// define the tasks
		// scala task
		Compile / sourceGenerators += idlGenerateScala,
		idlGenerateScala := {
			val pak = "S3" // organization.value + "." + name.value
			val src = idlRoots.value
			val out = idlTargetScala.value
			import peterlavalle.puregen._

			S3.Scala(pak, src, out)
		},
		// purescript generation tasks
		Compile / resourceGenerators += idlGenerateScript,
		idlGenerateScript := {
			val pak = "S3" // organization.value + "." + name.value
			val src = idlRoots.value
			val out = idlTargetScript.value
			import peterlavalle.puregen._
			S3.Script(pak, src, out)
		},
	)
}
