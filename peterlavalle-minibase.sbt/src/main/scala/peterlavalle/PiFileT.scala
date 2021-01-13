package peterlavalle

import java.io.{File, FileWriter, Writer}
import java.util

import scala.collection.immutable.Stream.Empty
import scala.io.{BufferedSource, Source}


trait PiFileT {

	implicit class PiFile(from: File) {
		val AbsoluteFile: File = from.getAbsoluteFile

		def **(p: String => Boolean): Stream[String] = {

			def loop(root: File): Stream[String] = {

				require(!root.isFile)

				require(root.isDirectory == root.exists())

				val list: Stream[String] =
					root
						.list() match {
						case null => Empty
						case list => list.toStream
					}

				list.filter(root / (_: String) isFile) ++ {
					list.map((name: String) => name -> root / name)
						.filterNot((_: (String, File))._2.isFile)
						.flatMap {
							case (name, file) =>
								loop(file.AbsoluteFile).map(name + '/' + (_: String))
						}
				}
			}

			loop(AbsoluteFile)
				.filter(p)
		}

		def ioWriteLines(text: String): File =
			ioWriteLines(text.split("[\r \t]*\n"))

		def ioWriteLines(lines: Seq[String]): File = {
			val file = AbsoluteFile

			require(file.exists() == file.isFile)

			lines.foldLeft(
				new FileWriter(file.EnsureParent).asInstanceOf[Writer]
			)((_: Writer) append (_: String) append "\n")
				.close()


			file
		}

		def /(path: String): File =
			if (path.startsWith("../"))
				AbsoluteFile.ParentFile / path.drop(3)
			else
				new File(AbsoluteFile, path).getAbsoluteFile

		def reWriteLine(regex: String)(make: String => String): Err[File] =
			Source.fromFile(AbsoluteFile).using {
				src: BufferedSource =>

					lazy val mkString: String = src.mkString

					@scala.annotation.tailrec
					def loop(todo: List[String], done: List[String]): Err[File] = {
						todo match {
							case Nil =>
								Err ! s"no lines match `$regex`"

							case line :: tail if line matches regex =>
								if (tail.exists((_: String) matches regex))
									Err ! s"multiple lines match `$regex`"
								else {
									tail.foldLeft {
										done
											.reverse
											.foldLeft(new FileWriter(AbsoluteFile): Writer)((_: Writer) append (_: String) append "\n")
											.append(make(line)).append("\n")
									}((_: Writer) append (_: String) append "\n").close()
									Err(AbsoluteFile)
								}

							case head :: tail =>
								loop(
									tail,
									head :: done
								)
						}
					}

					loop(
						mkString.split("[\r \t]*\n").toList,
						Nil
					)
			}

		def $(cmds: String*)(log: String => Unit, ret: Int = 0): Err[Unit] = {
			val out: Int = AbsoluteFile.$(cmds.toSeq, (o: String) => log(';' + o), (e: String) => log('!' + e))
			if (out == ret)
				Err(())
			else
				Err ! s"the command ${cmds.head} failed"
		}

		def $(cmds: Seq[String], out: String => Unit, err: String => Unit): Int = {
			require(cmds.nonEmpty)
			require(AbsoluteFile.isDirectory)
			import sys.process._
			Process(
				cmds,
				AbsoluteFile
			) ! ProcessLogger(out, err)
		}

		def EnsureParent: File = {
			require(ParentFile.EnsureMkDirs.exists())
			AbsoluteFile
		}

		def EnsureMkDirs: File = {
			if (!(AbsoluteFile.isDirectory || AbsoluteFile.mkdirs()))
				require(AbsoluteFile.isDirectory || AbsoluteFile.mkdirs())
			AbsoluteFile
		}

		def ParentFile: File = AbsoluteFile.getParentFile.getAbsoluteFile

		def FreshFolder: File = {
			require(!AbsoluteFile.isFile)
			if (AbsoluteFile.exists())
				AbsoluteFile.listFiles().foreach(_.Unlink)
			AbsoluteFile.EnsureMkDirs
		}

		def Unlink: Unit = {
			if (AbsoluteFile.isDirectory)
				AbsoluteFile.listFiles()
					.foreach(_.Unlink)
			require(AbsoluteFile.delete() || !AbsoluteFile.exists())
		}

		def AbsolutePath: String = AbsoluteFile.getAbsolutePath.replace('\\', '/')

		def * : Set[String] =
			AbsoluteFile.list() match {
				case null => Set()
				case list =>
					list.toSet
			}
	}

}
