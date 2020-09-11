package peterlavalle.puregen

import java.io.File

import peterlavalle.{Err, _}

import scala.io.{BufferedSource, Source}
import scala.util.matching.Regex

object SpagoBuild {

	val rDep: Regex = "import .*--\\s*dep\\s*:(.*)".r

	def apply[O](workIn: File)(source: File*)(act: File => O): Err[O] = {

		lazy val sources: Seq[File] = {
			val sources: Seq[File] =
				source
					.filter((_: File).isDirectory)
					.map((_: File).AbsoluteFile)
					.distinct

			if (sources.isEmpty) {
				println(">>>>")
				println(source.toList.map(_.AbsolutePath))
				println("<<")
				require(sources.nonEmpty, "there are no sources!?")
			}

			sources
		}

		for {

			// create the/a project (if missing)
			file <- {
				require(workIn.EnsureMkDirs.isDirectory)

				if ((workIn / "spago.dhall").isFile) {
					Err(workIn / "spago.dhall")
				} else {
					workIn.$(spago("init"): _ *)(System.err.println) ? {
						_: Unit =>
							workIn / "spago.dhall"
					}
				}
			}

			// overwrite the source dirs
			file <-
				file.reWriteLine(", sources = \\[ .* \\]") {
					_: String =>
						sources
							.foldLeft(", sources = [ ")((_: String) + "\"" + (_: File).AbsolutePath + "/**/*.purs\", ")
							.dropRight(2) + " ]"
				}

			// overwrite the dependency list
			file <-
				file.reWriteLine(", dependencies = \\[ .* \\]") {
					_: String =>
						val dependencies: Stream[String] =
							sources
								.toStream
								.flatMap {
									dir: File =>
										(dir ** ((_: String).endsWith(".purs")))
											.map(dir / (_: String))
											.flatMap {
												Source.fromFile(_: File).using {
													(_: BufferedSource)
														.mkString
														.split("\n")
														.map {
															case rDep(dep) =>
																dep.trim
															case _ =>
																"psci-support"
														}
														.distinct
												}
											}
											.distinct
								}
								.distinct
								.sorted

						(if (dependencies.nonEmpty)
							dependencies
						else {
							System.err.println("no dependencies found ... assuming just prelude")
							Stream("prelude")
						})
							.foldLeft(", dependencies = [ ")((_: String) + "\"" + (_: String) + "\", ")
							.dropRight(2) + " ]"
				}


		} {
			// compile it!
			val index: File = file.ParentFile / "index.js"
			require(!index.exists() || index.delete())

			file.ParentFile.$(spago("bundle-module"): _ *)(System.err.println) ? {
				_: Unit =>

					require(
						index.isFile,
						s"no index.js in `${file.ParentFile.AbsolutePath}`"
					)

					act {
						index
					}
			}
		}
	}

	def spago(cmd: String*): Seq[String] = {
		def path(name: String): String = {
			System.getenv("PATH").split(File.pathSeparator)
				.flatMap {
					path: String =>
						osNameArch {
							case ("windows", _) =>
								Seq(".exe", ".cmd", ".bat")
									.map(path + File.separatorChar + name + (_: String))
						}
				}.map(new File(_: String).AbsoluteFile)
				.filter((_: File).isFile())
				.head
				.AbsolutePath
		}

		Seq(path("spago"), "--no-color", "--quiet") ++ cmd
	}

}
