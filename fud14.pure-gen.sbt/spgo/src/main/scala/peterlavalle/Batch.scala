package peterlavalle

import java.io.{File, FileWriter}

trait Batch extends AutoCloseable {
	def run(log: String => Unit): Int = run(l => log(";" + l), l => log("!" + l))

	def run(out: String => Unit, err: String => Unit): Int

	def ++(cmd: Batch): Batch = ???
}

/**
 * to get around some sort of ... quirk ... in Windows, we'll write our commands to a batch file before running them
 *
 * ... and whatever the nix equivalent is
 */
object Batch {

	def apply(dir: File, cmd: String*): Batch = {
		val temp: File = {
			val temp: File =
				osNameArch {
					case ("windows", _) =>
						File.createTempFile("batch-" + cmd.toList.toString().md5, ".bat", dir)
				}

			new FileWriter(temp)
				.append("@ECHO OFF\n\n")
				.append(s"CD ${dir.AbsolutePath}\n\n")
				.append(
					cmd.foldLeft("")((_: String) + " " + (_: String)) + "\n\n"
				)
				.close()

			temp
		}

		require(temp.ParentFile == dir.AbsoluteFile)

		new Batch {
			override def run(out: String => Unit, err: String => Unit): Int = {
				require(temp.isFile)
				import sys.process._
				Process(
					Seq(temp.AbsolutePath),
					dir
				) ! ProcessLogger(out, err)
			}

			override def close(): Unit =
				require(temp.delete() && !temp.exists())
		}
	}

}
