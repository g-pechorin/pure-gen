package peterlavalle.puregen

import java.io.File

import S3.S3
import org.graalvm.polyglot.Context

import scala.io.{BufferedSource, Source}

object DemoTry {

	def main(args: Array[String]): Unit = {
		if (args.nonEmpty)
			???
		else
			runAgent()
	}

	private def runAgent(): Unit = {

		println("running with user.dir=`" + System.getProperty("user.dir") + "`")

		val demo: File =
			new File("demo").AbsoluteFile

		val spago: File =
			demo / "target/spago"

		require((spago / "gen").isDirectory, "you need to run sbt compile to generate the headers")

		//
		// setup the project and compile it
		val built: String = {
			SpagoBuild(spago)(
				demo.ParentFile / "lib",
				demo / "iai",
				spago / "gen"
			) {
				src: File =>

					// grab the source
					val str: String =
						Source.fromFile(src).using((_: BufferedSource).mkString)
							.trim

					// validate that this ends with the "module" thingie
					require(str.endsWith("module.exports = PS[\"Main\"];"), "the source didn't end as expected")

					// (sort of) rewrite the thing to get at the bits we want
					// ... but redundant (kind of)
					"(function(){" + str.dropRight("module.exports = PS[\"Main\"];".length) + "\nreturn PS;})()"
			}
		}

		//
		// now - actually launch the damn thing
		BuiltIn(built) {
			(context: Context, pedal: BuiltIn.Hook, run: Runnable) =>
				(new S3(context, pedal, run)
					with TrySphinx

					with TheAudio
					with TheGCASR
					with TheScenario

					with TryMary
					) ()
		}
	}
}
