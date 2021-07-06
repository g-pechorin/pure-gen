
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

name := "pure-gen"

//
// this bit of weirdness fixes an XSLT error in the CLI and IDEA
// this might need to be "inverted" for the/a
def maryXSLTFix = {
	// is this in the IntelliJ IDEA? ...
	if ("true" == System.getProperty("idea.managed"))
	// ... then addind the stuff below causes an error
		Seq()
	else
	// ... we're using the CLI and need these to "fix" and XSLT transformer error
		Seq(
			// https://github.com/marytts/marytts/issues/455
			"xalan" % "xalan" % "2.7.2",

			// https://github.com/marytts/marytts/issues/740
			"net.sf.saxon" % "Saxon-HE" % "9.7.0-18",
		)
}

val all: Seq[Def.Setting[_]] =
	Seq(
		scalaVersion := conf("scala.version"),
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % conf("scala.test") % Test,
		),
		resolvers += Classpaths.typesafeReleases,
		resolvers += Resolver.sonatypeRepo("public"),
		resolvers += Resolver.sonatypeRepo("snapshots"),
		resolvers += Resolver.sonatypeRepo("staging"),
		resolvers += Resolver.sonatypeRepo("releases"),
		resolvers += Resolver.jcenterRepo,

		// here's hoping that this'll fix that problem with
		scalacOptions ++= Seq("-Xmax-classfile-name", "128"),
	)

lazy val base = project
	.settings(resolvers += "jitpack" at "https://jitpack.io")
	// proxy project to get minibase
	.settings(libraryDependencies += "com.github.g-pechorin" % "minibase" % "cb5de70")

lazy val root = {
	(project in file("."))
		.settings(
			name := "pureGen"
		)
		.settings(
			all: _ *
		)
		.aggregate(
			base,
			core,
			demo,
			mary,
			spgo,
			// test,
			wson,
		)
}

lazy val core = {
	project
		.settings(all: _ *)
		.dependsOn(base)
		.settings(
			libraryDependencies ++= Seq(
				"org.graalvm.js" % "js" % "20.1.0"
			)
		)
}

lazy val demo = {
	project
		.settings(all: _ *)
		.dependsOn(
			base,
			core,
			mary,
			spgo,
			wson,
		)
}

lazy val mary = {
	project
		.settings(all: _ *)
		.dependsOn(
			base,
			core,
		)
		.settings(

			libraryDependencies ++= maryXSLTFix,

			libraryDependencies ++= Seq(
				"de.dfki.mary" % "voice-cmu-slt-hsmm" % "5.2",
				"de.dfki.mary" % "marytts-client" % "5.2",
				"de.dfki.mary" % "marytts-common" % "5.2",
			)
		)
}
lazy val spgo = {
	project
		.settings(all: _ *)
		.dependsOn(
			base,
			core % Test,
			// test % Test,
		)
		.dependsOn(RootProject(hgRoot / "puresand.sbt/"))
		.settings(
			libraryDependencies += "org.graalvm.js" % "js" % "20.1.0",
			Compile / resourceDirectory := (Compile / scalaSource).value,
		)
}
lazy val wson = {
	project
		.settings(all: _ *)
		.dependsOn(
			base,
			core,
		)
		.settings(
			libraryDependencies ++= Seq(
				// "com.ibm.watson" % "speech-to-text" % "8.5.0",

				"com.google.cloud" % "google-cloud-speech" % "1.24.0",
				"commons-cli" % "commons-cli" % "1.4" % Test,

				// "org.mozilla.deepspeech" % "libdeepspeech" % "0.8.1",
				// // "org.mozilla.deepspeech" % "libdeepspeech" % "0.7.4",

				// //
				// //			//
				// //			//			//
				// //			//			"org.springframework" % "spring-websocket" % "5.2.2.RELEASE",
				// //			//			"org.springframework" % "spring-messaging" % "5.2.2.RELEASE",
				// //			//			"javax.websocket" % "javax.websocket-api" % "1.0", // needed on own?
				// //

				// // trying to hack blue
				// "org.java-websocket" % "Java-WebSocket" % "1.5.1",

				// show sphinx doing the thing
				"edu.cmu.sphinx" % "sphinx4-core" % "5prealpha-SNAPSHOT",
				"edu.cmu.sphinx" % "sphinx4-data" % "5prealpha-SNAPSHOT",

			)
		)
}
