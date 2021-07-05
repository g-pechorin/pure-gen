package peterlavalle.puregen

import java.io.File

object TestTemp {
	def apply[O](act: File => O): O = {

		// make a folder that's a bit unique
		val temp: File = {
			"target" / {
				"temp-" + Thread.currentThread().getStackTrace
					.foldLeft("")((_: String) + (_: StackTraceElement))
					.md5
					.take(4)
			}
		}.EnsureMkDirs

		require(temp.isDirectory)


		// compute whatever value needed that folder
		val out: O =
			try {
				act(temp)
			} catch {

				case e: Throwable =>

					// dump out the exception

					System.err.flush()
					System.out.flush()

					e.printStackTrace(System.err)
					System.err.flush()

					//
					val exception = new Exception("in temp " + temp.AbsolutePath, e)

					// mega-shorten the stack trace
					exception.setStackTrace(exception.getStackTrace.drop(1).take(1))

					throw exception
			}

		// only delete if we worked
		temp.Unlink

		out
	}
}
