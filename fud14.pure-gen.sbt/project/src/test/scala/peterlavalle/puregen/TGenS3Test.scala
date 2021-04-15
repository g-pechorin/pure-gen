package peterlavalle.puregen


trait TGenS3Test extends TGenTest {

	lazy val srcPureScript: String =
		bind("expected.purs") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")
	lazy val srcJavaScript: String =
		bind("expected.js") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	test("compute scala modules - combined") {

		val stuff: Map[String, Stream[String]] =
			S3.Scala.translate(pack, Set(module))

		val scala: String = pack.replace(".", "/") + "/" + getClass.getSimpleName + ".scala"
		val index: String = pack.replace(".", "/") + "/S3.scala"
		assert(2 == stuff.size)


		assert(stuff keySet scala)
		assert(stuff keySet index)

		srcScala assertSourceEqual stuff(scala).foldLeft("")((_: String) + (_: String) + "\n")
		srcIndex assertSourceEqual stuff(index).foldLeft("")((_: String) + (_: String) + "\n")
	}

	test("compute scala module - scala") {

		val stuff: Map[String, Stream[String]] =
			S3.Scala.ScalaClasses.translate(pack, Set(module))

		val scala: String = pack.replace(".", "/") + "/" + getClass.getSimpleName + ".scala"
		assert(1 == stuff.size)

		assert(stuff keySet scala)

		srcScala assertSourceEqual stuff(scala).foldLeft("")((_: String) + (_: String) + "\n")
	}

	test("compute scala module - index") {

		val stuff: Map[String, Stream[String]] =
			S3.Scala.IndexClasses.translate(pack, Set(module))

		val index: String = pack.replace(".", "/") + "/S3.scala"
		assert(1 == stuff.size)


		assert(stuff keySet index)

		srcIndex assertSourceEqual stuff(index).foldLeft("")((_: String) + (_: String) + "\n")
	}

	// scala is different - because of the source formatting we assume it
	def srcScala: String =
		bind("expected.scala.txt") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	def srcIndex: String = {
		// this is also just a scala file
		bind("expected.index.txt") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")
	}

	test("compute pure-script module") {

		val expected: String = srcPureScript

		val actual: String =
			pursGeneration
				.foldLeft("")((_: String) + (_: String) + "\n")

		expected assertSourceEqual actual
	}

	/**
	 * the new generators work differently, but. we can just overide part of the test cases to use them here
	 */
	def pursGeneration: Stream[String] = {
		val want = pack.replace(".", "/") + "/" + getClass.getSimpleName + ".purs"
		val List((name, data)) =
			S3.Script.PureScript.translate(pack, Set(module))
				.toList

		assert(name == want)

		data
	}

	def ecmaGeneration: Stream[String] = {
		val want = pack.replace(".", "/") + "/" + getClass.getSimpleName + ".js"
		val List((name, data)) =
			S3.Script.JavaScript.translate(pack, Set(module))
				.toList

		assert(name == want)

		data
	}

	test("compute java-script module") {
		val expected: String = srcJavaScript

		val actual: String =
			ecmaGeneration
				.foldLeft("")((_: String) + (_: String) + "\n")

		expected assertSourceEqual actual
	}

}
