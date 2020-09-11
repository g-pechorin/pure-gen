package peterlavalle.puregen

trait TGenPureScriptTest extends TGenTest {

	lazy val srcPureScript: String =
		bind("expected.purs") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	test("compute pure-script module") {
		val actual: String =
			PureScriptModule(pack, getClass.getSimpleName, module)
				.foldLeft("")((_: String) + (_: String) + "\n")

		srcPureScript assertSourceEqual actual
	}
}
