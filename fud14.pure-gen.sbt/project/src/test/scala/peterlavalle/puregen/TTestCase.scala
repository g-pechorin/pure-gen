package peterlavalle.puregen

import java.io.File

import org.scalatest.funsuite.AnyFunSuite

trait TTestCase extends AnyFunSuite {

	private lazy val target: File = new File("target").getAbsoluteFile

	implicit class PiTree(any: Any) {
		def assertSourceEqual(actual: String): Unit = {

			lazy val traceCode: String =
				TTestCase.this.getClass.getName + Math.abs(
					(Thread.currentThread().getStackTrace.toList.foldLeft("")(_ + _))
						.hashCode
				).toString

			val expected: String = any.toString

			val ev: String = expected.replaceAll("([\t \r]*\n)+", "\n").trim
			val av: String = actual.replaceAll("([\t \r]*\n)+", "\n").trim

			if (ev != av) {

				System.err.println(
					"kdiff3 " + (target / (traceCode + ".expected"))
						.ioWriteLines(expected)
						.AbsolutePath + " " +
						(target / (traceCode + ".actual"))
							.ioWriteLines(actual)
							.AbsolutePath
				)


				assert(av == ev)
			}
		}
	}

	def module: IR.Module
}
