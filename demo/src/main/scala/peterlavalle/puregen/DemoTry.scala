package peterlavalle.puregen

import java.io.File

import S3.S3
import org.graalvm.polyglot.Context

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
		// setup our compiler
		object Log extends SpagoCompile.Log {
			override def out(line: Any): Unit = {
				flush()
				System.out.println(line)
			}

			def flush(): Unit = {
				System.out.flush()
				System.err.flush()
			}

			override def err(line: Any): Unit = {
				flush()
				System.err.println(line)
			}
		}

		val compile = new SpagoCompile(spago)

		// compile has some sources
		compile.sources.add(demo.ParentFile / "lib")
		compile.sources.add(demo / "iai")
		compile.sources.add(spago / "gen")

		compile.autoDependencies()

		import SpagoCompile._
		val built: String =
			compile.bundleModule(Log, "Main")
				.map(_.trim)
				.map {
					str =>
						// validate that this ends with the "module" thingie
						require(str.endsWith("module.exports = PS[\"Main\"];"), "the source didn't end as expected")

						// (sort of) rewrite the thing to get at the bits we want
						// ... but redundant (kind of)
						"(function(){" + str.dropRight("module.exports = PS[\"Main\"];".length) + "\nreturn PS;})()"

				}
				.value

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
