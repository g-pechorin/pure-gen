package peterlavalle.puregen

import java.io.File
import java.nio.file.Files

object TestTemp {
	def apply[O](act: File => O): O = {
		val temp: File =
			Files.createTempDirectory(
				(new File("target") / "test").EnsureMkDirs.toPath,
				getClass.getName
			).toFile

		require(temp.isDirectory)


		val out = act(temp)
		// only delet if we worked
		temp.Unlink
		out
	}
}
