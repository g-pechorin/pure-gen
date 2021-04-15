package peterlavalle.puregen

import java.io.File

import peterlavalle.{Batch, _}

import scala.io.{BufferedSource, Source}
import scala.util.matching.Regex

object SpagoBuild {

	val rDep: Regex = "import .*--\\s*dep\\s*:(.*)".r

	def apply[O](
								workIn: File
							)(
								source: File*
							)(
								actionOnResult: File => O
							): O = {

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

		// create the/a project (if missing)
		val projectDhall: File = {
			require(workIn.EnsureMkDirs.isDirectory)

			if ((workIn / "spago.dhall").isFile) {
				workIn / "spago.dhall"
			} else {

				Batch(workIn, spago("init"): _ *)
					.using(
						(_: Batch)
							.run(System.err.println) match {
							case 0 =>
								workIn / "spago.dhall"
						}
					)
			}
		}

		// rewrite the file's source dirs
		projectDhall.reWriteLine(", sources = \\[ .* \\]") {
			_: String =>
				sources
					.foldLeft(", sources = [ ")((_: String) + "\"" + (_: File).AbsolutePath + "/**/*.purs\", ")
					.dropRight(2) + " ]"
		}

		// overwrite the dependencies based on stuff matched from the source files
		projectDhall.reWriteLine(", dependencies = \\[ .* \\]") {
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

		// ensure that the file doesn't already exist
		val index: File = projectDhall.ParentFile / "index.js"
		require(!index.exists() || index.delete())

		//
		Batch(
			projectDhall.ParentFile,
			"spago", "--no-color", // "--quiet",
			"bundle-module"
		).using(
			(_: Batch).run(System.err.println(_: String))
		) match {
			case 0 =>
				require(
					index.isFile,
					s"build completed, but, no index.js in `${projectDhall.ParentFile.AbsolutePath}`"
				)

				actionOnResult {
					index
				}
		}
	}

	/**
	 * hacky helper to build the/a spago command.
	 *
	 * this feels less graceful since i started using the Batch class, but, still needed (sort of)
	 *
	 * ... maybe it should "encap" the Batch construction?
	 */
	def spago(cmd: String*): Seq[String] =
		Seq("npx", "spago", "--no-color", "--quiet") ++ cmd

}
