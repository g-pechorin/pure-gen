package peterlavalle.puregen

import org.graalvm.polyglot.{Context, HostAccess, PolyglotException, Value}

import scala.reflect.ClassTag

/**
 * generated elsewhere
 *
 * the `.orig` is the generotr's output, copy it to the .scala
 */
object includeT {

	trait ScriptedGen {
		def scripted[O](f: (() => O)): (() => O) =
			new (() => O) {
				@HostAccess.Export
				override def apply(): O = f()
			}

		def scripted[A0 <: AnyRef : ClassTag, O](f: ((A0) => O)): ((A0) => O) =
			new ((A0) => O) {
				@HostAccess.Export
				override def apply(v0: A0): O = f(v0)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, O](f: ((A0, A1) => O)): ((A0, A1) => O) =
			new ((A0, A1) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1): O = f(v0, v1)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, O](f: ((A0, A1, A2) => O)): ((A0, A1, A2) => O) =
			new ((A0, A1, A2) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2): O = f(v0, v1, v2)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3) => O)): ((A0, A1, A2, A3) => O) =
			new ((A0, A1, A2, A3) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2, v3: A3): O = f(v0, v1, v2, v3)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4) => O)): ((A0, A1, A2, A3, A4) => O) =
			new ((A0, A1, A2, A3, A4) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4): O = f(v0, v1, v2, v3, v4)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5) => O)): ((A0, A1, A2, A3, A4, A5) => O) =
			new ((A0, A1, A2, A3, A4, A5) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5): O = f(v0, v1, v2, v3, v4, v5)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5, A6) => O)): ((A0, A1, A2, A3, A4, A5, A6) => O) =
			new ((A0, A1, A2, A3, A4, A5, A6) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6): O = f(v0, v1, v2, v3, v4, v5, v6)
			}

		def scripted[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, A7 <: AnyRef : ClassTag, O](f: ((A0, A1, A2, A3, A4, A5, A6, A7) => O)): ((A0, A1, A2, A3, A4, A5, A6, A7) => O) =
			new ((A0, A1, A2, A3, A4, A5, A6, A7) => O) {
				@HostAccess.Export
				override def apply(v0: A0, v1: A1, v2: A2, v3: A3, v4: A4, v5: A5, v6: A6, v7: A7): O = f(v0, v1, v2, v3, v4, v5, v6, v7)
			}
	}

	trait ScriptedValue {
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

			require(call.canExecute)

			(v0: A0) =>
				val eff: Value = call.execute(v0)

				require(
					eff.toString.startsWith("function __do() {\n"),
					"it wasn't a PS Eff"
				)

				// do notation requires another call ... because
				val open: Value =
					try {
						eff
							.execute()
					} catch {
						case e: PolyglotException if e.getMessage == "TypeError: v.value0(...) is not a function" =>
							e ! "something that was supposed to be a monad but was not"
					}

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

	trait ScriptedContext {
		def context: Context


		/**
		 * binds some n-arg function to a global path
		 */
		def global[O](path: String)(f: () => O): Context = {

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
					 |	into['${full.last}'] = (() => call.apply());
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, O](path: String)(f: (A0) => O): Context = {

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
					 |	into['${full.last}'] = ((v0) => call.apply(v0));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1) => call.apply(v0, v1));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2) => call.apply(v0, v1, v2));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2, A3) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2, v3) => call.apply(v0, v1, v2, v3));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2, A3, A4) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2, v3, v4) => call.apply(v0, v1, v2, v3, v4));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2, A3, A4, A5) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2, v3, v4, v5) => call.apply(v0, v1, v2, v3, v4, v5));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2, A3, A4, A5, A6) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2, v3, v4, v5, v6) => call.apply(v0, v1, v2, v3, v4, v5, v6));
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

		/**
		 * binds some n-arg function to a global path
		 */
		def global[A0 <: AnyRef : ClassTag, A1 <: AnyRef : ClassTag, A2 <: AnyRef : ClassTag, A3 <: AnyRef : ClassTag, A4 <: AnyRef : ClassTag, A5 <: AnyRef : ClassTag, A6 <: AnyRef : ClassTag, A7 <: AnyRef : ClassTag, O](path: String)(f: (A0, A1, A2, A3, A4, A5, A6, A7) => O): Context = {

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
					 |	into['${full.last}'] = ((v0, v1, v2, v3, v4, v5, v6, v7) => call.apply(v0, v1, v2, v3, v4, v5, v6, v7));
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
