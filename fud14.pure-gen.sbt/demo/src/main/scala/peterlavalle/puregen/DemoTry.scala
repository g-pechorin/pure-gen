package peterlavalle.puregen

import java.io.File

import org.graalvm.polyglot.{Context, Value}
import pdemo.{Mary, Scenario, Sphinx}

import scala.io.{BufferedSource, Source}
import scala.reflect.ClassTag

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

		val spago =
			demo / "target/spago"

		require((spago / "gen").isDirectory, "you need to run sbt compile to generate the headers")

		val built: String =
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
			} need "needed to compile that"

		Context.create() using {
			context: Context =>

				context.global[Any, Unit]("println")(println)

				val starter: Starter = Starter.await()

				val cyclist = new Cyclist(starter)

				// build up the scripting things
				if (true) {
					new pidl.A with pidl.B {

						override val bike: Cyclist = cyclist

						import TModule._

						override lazy val pdemo_Scenario: Scenario = new TheScenario()
						override lazy val pdemo_Sphinx: Sphinx = new TrySphinx()
						override lazy val pdemo_Mary: Mary = new TryMary()

						override def s[I: ClassTag](sample: Sample[I]): () => Any =
							sample match {
								case sample: TModule.ReadSample[I] =>
									cyclist.sample(sample.read)
							}

						override def e[I: ClassTag](event: Event[I]): () => Any =
							event match {
								case triggerEvent: TriggerEvent[I, Event[I]] =>
									e(triggerEvent.action(starter))

								case readEvent: ReadEvent[I] =>
									TODO("bad cast")
									cyclist.event(readEvent.read.asInstanceOf[() => Option[AnyRef]])
							}

						override def o[O: ClassTag](signal: Signal[O]): Any => Unit =
							signal match {
								case signal: TModule.SendSignal[O] =>
									cyclist.signal(signal.send)
							}


					}.apply(context)
				}

				lazy val scriptValue: Value = context.eval("js", built)

				lazy val cycle: Value => Value =
					scriptValue
						.eff[Value, Value]("Main.cycle")

				daemon {

					def fail(status: Int, e: Exception, m: String): Exception = {
						System.out.flush()
						System.out.close()
						// excuse the wall of text; not all modules want to close, so, I need/want a way to ensure this is seen
						System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
						System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
						System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
						System.err.println("! ")
						System.err.println("! " + e.getMessage)
						System.err.println("! ")
						System.err.println("======================================")
						System.err.println("======================================")
						e.printStackTrace(System.err)
						System.err.println("======================================")
						System.err.println("\n" + m + "\n")
						System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
						System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
						System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
						System.exit(status)
						e
					}

					var last: Value =
						try {
							scriptValue
								.eff[Null, Value]("Main.agent")
								.apply(null)
						} catch {
							case e: Exception =>
								throw fail(-1, e, "caught an exception during the setup")
						}
					while (starter()) {
						try {
							cyclist.load()
							last = cycle(last)
							cyclist.send()
						} catch {
							case e: Exception =>
								throw fail(-2, e, "caught an exception during the cycle")
						}
					}
				}

				starter.run()

				okayLoop("CONTROL" -> "tick the system") {
					starter.run()
				}

				starter.close()

				println("exiting happily")
		}
	}
}
