package peterlavalle.puregen

import java.io.File

object SpagoCompileDemo {

	val hello: String =
		"""
			|module Hello where
			|
			|import Prelude
			|import Effect.Console (log) -- dep: console
			|
			|greet :: String -> String
			|greet name = "Hello, " <> name <> "!"
			|
			|main = log (greet "World")
			|
			""".stripMargin.trim

	def main(args: Array[String]): Unit = {

		val compile = new SpagoCompile(???, File.createTempFile("spc.", ".bld", new File("target")))

		compile.gen(
			hello,
		)
		error("now what?")

	}
}
