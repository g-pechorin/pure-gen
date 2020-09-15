package peterlavalle.puregen

import java.io.File

import org.graalvm.polyglot.{Context, Value}
import pdemo.{Mary, Scenario, Sphinx}

import scala.io.{BufferedSource, Source}
import scala.reflect.ClassTag

object DemoTry extends App {

	println(System.getProperty("user.dir"))

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

					lazy val start: Long = System.currentTimeMillis()

					def age: Float =
						((System.currentTimeMillis() - start) * 0.001)
							.toFloat

					import TModule._

					override lazy val pdemo_Scenario: Scenario = new TryScenario(age)
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
				scriptValue.eff[Value, Value]("Main.cycle")

			daemon {
				var last: Value =
					scriptValue
						.eff[Null, Value]("Main.agent")
						.apply(null)

				while (starter()) {
					cyclist.load()
					last = cycle(last)
					cyclist.send()
				}
			}

			okayLoop("CONTROL" -> "tick the system") {
				starter.run()
			}

			starter.close()

			println("exiting happily")
	}
}