import java.io.File

import sbt.Def


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



















name := "pure-gen"
organization := "com.peterlavalle"
//version := "0.1.0-SNAPSHOT"
scalaVersion := conf("scala.version")

resolvers += Resolver.mavenCentral

resolvers += Classpaths.typesafeReleases
resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("staging")
resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.jcenterRepo

//https://repo.spring.io/plugins-release/


val all: Seq[Def.Setting[_]] =
	Seq(
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % conf("scala.test") % Test,
		),
		resolvers += Classpaths.typesafeReleases,
		resolvers += Resolver.sonatypeRepo("public"),
		resolvers += Resolver.sonatypeRepo("snapshots"),
		resolvers += Resolver.sonatypeRepo("staging"),
		resolvers += Resolver.sonatypeRepo("releases"),
		resolvers += Resolver.jcenterRepo,
	)




lazy val root =
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
			wson,
		)
		.dependsOn(
			base % "compile->test",
			core % "compile->test",
			demo % "compile->test",
			mary % "compile->test",
			spgo % "compile->test",
			wson % "compile->test",
		)

lazy val base = RootProject(hgRoot / "peterlavalle-minibase.sbt")

lazy val core = project
	.settings(all: _ *)
	.dependsOn(base)
	.settings(
		libraryDependencies ++= Seq(
			"org.graalvm.js" % "js" % "20.1.0"
		)
	)

lazy val demo = project
	.settings(all: _ *)
	.dependsOn(
		base,
		core,
		mary,
		spgo,
		wson,
	)

lazy val mary = project
	.settings(all: _ *)
	.dependsOn(
		base,
		core,
	)
	.settings(

		//
		// trying to fix mary xslt error
		libraryDependencies ++= Seq(

			//			//
			//			// these two fix issues discussed in threads BUT they break IDEA (which doesn't have the issues)
			//			// https://github.com/marytts/marytts/issues/455
			//			"xalan" % "xalan" % "2.7.2",
			//
			//			// https://github.com/marytts/marytts/issues/740
			//			"net.sf.saxon" % "Saxon-HE" % "9.7.0-18",

		),
		// end (of mary xslt error)
		//

		libraryDependencies ++= Seq(
			"de.dfki.mary" % "voice-cmu-slt-hsmm" % "5.2",
			"de.dfki.mary" % "marytts-client" % "5.2",
			"de.dfki.mary" % "marytts-common" % "5.2",
		)
	)

lazy val spgo = project
	.settings(all: _ *)
	.dependsOn(
		base,
		core % Test,
	)
	.settings(
		libraryDependencies += "org.graalvm.js" % "js" % "20.1.0",
	)

lazy val wson = project
	.settings(all: _ *)
	.dependsOn(
		base,
		core,
	)
	.settings(
		libraryDependencies ++= Seq(
			"com.ibm.watson" % "speech-to-text" % "8.5.0",

			"com.google.cloud" % "google-cloud-speech" % "1.24.0",
			"commons-cli" % "commons-cli" % "1.4" % Test,

			"org.mozilla.deepspeech" % "libdeepspeech" % "0.8.1",
			// "org.mozilla.deepspeech" % "libdeepspeech" % "0.7.4",

			//
			//			//
			//			//			//
			//			//			"org.springframework" % "spring-websocket" % "5.2.2.RELEASE",
			//			//			"org.springframework" % "spring-messaging" % "5.2.2.RELEASE",
			//			//			"javax.websocket" % "javax.websocket-api" % "1.0", // needed on own?
			//

			// trying to hack blue
			"org.java-websocket" % "Java-WebSocket" % "1.5.1",

			// show sphinx doing the thing
			"edu.cmu.sphinx" % "sphinx4-core" % "5prealpha-SNAPSHOT",
			"edu.cmu.sphinx" % "sphinx4-data" % "5prealpha-SNAPSHOT",

		)
	)
