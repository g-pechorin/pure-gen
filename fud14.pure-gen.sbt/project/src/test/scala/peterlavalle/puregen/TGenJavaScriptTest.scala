package peterlavalle.puregen

trait TGenJavaScriptTest extends TGenTest {

	lazy val srcJavaScript: String =
		bind("expected.js") {
			_: String =>
				error("there shouldn't be any blanks")
		}.foldLeft("")((_: String) + (_: String) + "\n")

	test("compute java-script module") {
		val actual: String =
			JavaScriptModule(pack, getClass.getSimpleName, module)
				.foldLeft("")((_: String) + (_: String) + "\n")

		srcJavaScript assertSourceEqual actual
	}
}
