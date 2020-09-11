package peterlavalle.puregen

import java.io.InputStream

import scala.io.{BufferedSource, Source}

trait TParseTest extends TTestCase {

	lazy val src: String = {
		val file: String = getClass.getSimpleName + ".pidl"
		val resourceStream: InputStream = getClass.getResourceAsStream(file)
		if (null == resourceStream)
			fail(
				s"no resource file `$file`"
			)
		assert(null != resourceStream)
		try {
			val source: BufferedSource = Source.fromInputStream(resourceStream)
			try {
				source.mkString
			} finally {
				source.close()
			}
		} finally {
			resourceStream.close()
		}
	}

	test("parse test") {

		import fastparse._

		new PCG.C1(getClass.getSimpleName) {} apply src match {
			case Parsed.Success(value: IR.Module, _) =>
				if (null == module)
					fail("that parse should not have worked")
				else
					assert(
						value == module
					)

			case f: Parsed.Failure =>
				if (module != null)
					fail(f.toString())
		}
	}
}
