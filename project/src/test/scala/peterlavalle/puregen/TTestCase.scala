package peterlavalle.puregen

import java.io.File

import org.scalatest.funsuite.AnyFunSuite

trait TTestCase extends AnyFunSuite {

	private lazy val target: File = new File("target").getAbsoluteFile

	implicit class PiTree(any: Any) {
		def assertSourceEqual(actual: String): Unit = {

			lazy val traceCode: String =
				TTestCase.this.getClass.getName + Math.abs(
					Thread.currentThread()
						.getStackTrace
						.toList
						.foldLeft("")((_: String) + (_: StackTraceElement))
						.hashCode
				).toString

			val expected: String = any.toString

			val ev: String = expected.replaceAll("([\t \r]*\n)+", "\n").trim
			val av: String = actual.replaceAll("([\t \r]*\n)+", "\n").trim

			val ef: File = target / (traceCode + ".expected")
			val af: File = target / (traceCode + ".actual")

			lazy val ep: String =
				ef
					.ioWriteLines(expected)
					.AbsolutePath

			lazy val ap: String =
				af
					.ioWriteLines(actual)
					.AbsolutePath

			if (ef.exists() || af.exists())
				ep + ap

			if (ev != av) {
				System.out.flush()
				System.err.println(
					"kdiff3 " + ep + " " + ap
				)
				System.err.flush()
				assert(av == ev)
			}
		}
	}

	def module: IR.Module
}
