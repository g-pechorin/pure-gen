
//import sbt.Keys._
//import sbt._

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
	val pureIn = peterlavalle.puregen.PureIn(idlRoots.value)
	pureIn(idlTargetScala.value, peterlavalle.puregen.PureIn.Scala)
}
// purescript generation tasks
Compile / resourceGenerators += idlGenerateScript
idlGenerateScript := {
	val pureIn =  peterlavalle.puregen.PureIn(idlRoots.value)
	pureIn(idlTargetScript.value, peterlavalle.puregen. PureIn.PureScript)
}
