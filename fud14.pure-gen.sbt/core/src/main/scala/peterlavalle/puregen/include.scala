package peterlavalle.puregen

import java.io.{File, InputStream}
import java.util.concurrent.atomic.AtomicBoolean

import org.graalvm.polyglot.{Context, HostAccess, Value}

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import scala.reflect.ClassTag

object include {

	/**
	 * generated elsewhere
	 */
	sealed trait ScriptedGen {


		def scripted[O](f: (() => O)): (() => O) = new (() => O) {
			@HostAccess.Export
			override def apply(): O = f()
		}


		def scripted[A0 <: AnyRef : ClassTag, O](f: ((A0) => O)): ((A0) => O) = new ((A0) => O) {
			@HostAccess.Export
			override def apply(v0: A0): O = f(v0)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, O](f: ((A0, A1) => O)): ((A0, A1) => O) = new ((A0, A1) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1): O = f(v0, v1)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, O](f: ((A0, A1, A2) => O)): ((A0, A1, A2) => O) = new ((A0, A1, A2) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2): O = f(v0, v1, v2)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3) => O)): ((A0, A1, A2, A3) => O) = new ((A0, A1, A2, A3) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2, v3: A3): O = f(v0, v1, v2, v3)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4) => O)): ((A0, A1, A2, A3, A4) => O) = new ((A0, A1, A2, A3, A4) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4): O = f(v0, v1, v2, v3, v4)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5) => O)): ((A0, A1, A2, A3, A4, A5) => O) = new ((A0, A1, A2, A3, A4, A5) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5): O = f(v0, v1, v2, v3, v4, v5)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5, A6) => O)): ((A0, A1, A2, A3, A4, A5, A6) => O) = new ((A0, A1, A2, A3, A4, A5, A6) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6): O = f(v0, v1, v2, v3, v4, v5, v6)
		}


		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, A7 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5, A6, A7) => O)): ((A0, A1, A2, A3, A4, A5, A6, A7) => O) = new ((A0, A1, A2, A3, A4, A5, A6, A7) => O) {
			@HostAccess.Export
			override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6, v7: A7): O = f(v0, v1, v2, v3, v4, v5, v6, v7)
		}

	}

	sealed trait ScriptedValue {
		def find(path: String*): Value


		def eff[O <: AnyRef : ClassTag](name: String): () => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			() =>
				val eff: Value = call.execute()

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(null != call, s"failed to find `$name``")

			require(call.canExecute)

			(v0: A0) =>
				val eff: Value = call.execute(v0)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1) =>
				val eff: Value = call.execute(v0, v1)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2) =>
				val eff: Value = call.execute(v0, v1, v2)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2, A3) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2, v3: A3) =>
				val eff: Value = call.execute(v0, v1, v2, v3)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2, A3, A4) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4) =>
				val eff: Value = call.execute(v0, v1, v2, v3, v4)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2, A3, A4, A5) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5) =>
				val eff: Value = call.execute(v0, v1, v2, v3, v4, v5)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2, A3, A4, A5, A6) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6) =>
				val eff: Value = call.execute(v0, v1, v2, v3, v4, v5, v6)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}


		def eff[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, A7 <: AnyRef : ClassTag, O <: AnyRef : ClassTag](name: String): (A0, A1, A2, A3, A4, A5, A6, A7) => O = {

			val call: Value =
				find(
					name.split("\\."): _ *
				)

			require(call.canExecute)

			(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6, v7: A7) =>
				val eff: Value = call.execute(v0, v1, v2, v3, v4, v5, v6, v7)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value = eff.execute()

				// cast it ... yay?
				open.asInstanceOf[O]
		}

	}

}

trait include
	extends include.ScriptedGen {

	@tailrec
	final def loop[V](get: => V)(test: V => Boolean)(act: V => Unit): Unit = {
		val v: V = get
		if (test(v)) {
			act(v)
			loop(get)(test)(act)
		}
	}

	implicit class PiInputStream4(stream: InputStream) {
		def capacitor(len: Int)(into: Array[Byte] => Unit): AutoCloseable =
			new AutoCloseable {

				val live = new AtomicBoolean(true)

				val work: AutoCloseable =
					daemon {
						val bytes: Array[Byte] = Array.ofDim[Byte](len)

						loop(stream.read(bytes))(_ != -1 && live.get()) {
							read: Int =>
								assume(0 <= read)
								if (0 != read)
									into(bytes.clone().take(read))
						}
					}


				override def close(): Unit = {
					live.set(false)
					work.close()
				}
			}
	}


	implicit class PiValue(value: Value) extends include.ScriptedValue {

		def find(path: String*): Value =
			path.toList match {
				case Nil => value
				case head :: tail =>
					value.getMember(head).find(tail: _ *)
			}

	}


	/**
	 * "narrowing" extensions to the GraalVM context
	 */
	implicit class PiContext(context: Context) {
		def module(file: File): Value =
			Source.fromFile(file).using {
				src: BufferedSource =>
					module(src.mkString)
			}

		/**
		 * runs a module (locally?) and returns the "exports" table.
		 */
		def module(src: String): Value =
			context.eval("js", "var module = {};\n" + src + "\nmodule.exports;")

		/**
		 * binds some one-arg function to a global path
		 *
		 * @param path the.path.to where the function should be bound
		 * @param f    the
		 * @tparam A0 arg0's type
		 * @tparam O  the return type
		 * @return the context (for chaining)
		 */
		def global[A0, O](path: String)(f: A0 => O): Context = {

			val full: List[String] =
				path.split("\\.")
					.toList

			val into: String =
				full.reverse.tail.reverse
					.map((name: String) => s"\tinto = (into['$name'] || (into['$name'] = {}));\n")
					.foldLeft("")((_: String) + (_: String))

			val script: String =
				s"""
					 |(call => {
					 |	var into = this;$into
					 |	into['${full.last}'] = (a0 => call.apply(a0));
					 |})
  			""".stripMargin

			context
				.eval(
					"js",
					script
				)
				.execute(scripted(f))
			context
		}
	}

}
