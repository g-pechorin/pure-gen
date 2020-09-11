package peterlavalle.puregen

trait TGenPureInTest extends TGenTest {

	// scala is different - because of the source formatting we assume it
	def srcPureIn: String =
		bind("expected.pureIn.scala.txt") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	test("compute pure-in module") {
		val actual: String =
			PureIn.Scala(pack, Seq(module))
				.foldLeft("")((_: String) + (_: String) + "\n")

		srcPureIn assertSourceEqual actual
	}
}
