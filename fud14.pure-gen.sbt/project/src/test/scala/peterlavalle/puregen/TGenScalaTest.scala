package peterlavalle.puregen

trait TGenScalaTest extends TGenTest {

	// scala is different - because of the source formatting we assume it
	def srcScala: String =
		bind("expected.scala.txt") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	test("compute scala module") {
		val actual: String =
			ScalaModule(pack, getClass.getSimpleName, module)
				.foldLeft("")((_: String) + (_: String) + "\n")

		srcScala assertSourceEqual actual
	}
}
