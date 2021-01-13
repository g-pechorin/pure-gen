package peterlavalle.puregen

import java.io.{File, InputStream}
import java.util.concurrent.atomic.AtomicBoolean

import org.graalvm.polyglot.{Context, Value}

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}

trait include
	extends includeT.ScriptedGen {

	@tailrec
	final def loop[V](get: => V)(test: V => Boolean)(act: V => Unit): Unit = {
		val v: V = get
		if (test(v)) {
			act(v)
			loop(get)(test)(act)
		}
	}



	implicit class PiValue(value: Value) extends includeT.ScriptedValue {
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
	implicit class PiContext(val context: Context) extends includeT.ScriptedContext {
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
	}

}
