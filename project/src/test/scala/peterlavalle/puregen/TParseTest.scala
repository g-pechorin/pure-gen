package peterlavalle.puregen

import java.io.InputStream

import peterlavalle.E

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


	val includeModule: String => IR.Module = {
		name: String =>
			fail(s"you tried to include `$name` but test ${getClass.getSimpleName} doesn't provide that")
	}

	test("parse test") {


		PCG(includeModule, getClass.getSimpleName, src) match {
			case E.Success(actual) =>
				assert(
					actual == module
				)
			case E.Failure(f) =>
				if (module != null)
					fail(f)
		}
	}
}
