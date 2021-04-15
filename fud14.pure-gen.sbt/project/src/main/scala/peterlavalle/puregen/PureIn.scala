package peterlavalle.puregen

import java.io.File

import peterlavalle.{DepCir, E, TemplateResource}

import scala.io.{BufferedSource, Source}

object PureIn extends TemplateResource {

	private val ext = ".pidl"

	def parse(src: Seq[File]): Seq[IR.Module] = {
		val tuples: Stream[(File, String)] =
			src
				.flatMap {
					src: File =>
						(src ** ((_: String).endsWith(ext)))
							.sorted
							.map(src -> (_: String))
				}
				.toStream
				.distinctBy((_: (File, String))._2)


		def load(name: String)(look: String => IR.Module): IR.Module = {
			val Stream(root) =
				tuples
					.filter((_: (File, String))._2 == name)
					.map((_: (File, String))._1)

			Source.fromFile(root / name).using {
				src: BufferedSource =>
					assume(name.endsWith(ext))

					PCG(
						look,
						name.dropRight(ext.length).replace('/', '.'),
						src.mkString
					) match {
						case E.Success(v) =>
							v
					}
			}
		}

		DepCir
			.tossCircle(tuples.map((_: (File, String))._2)) {
				(done: String => IR.Module) =>
					(name: String) =>
						load(name)((want: String) => done(want + ext))
			}
			.values
			.toSeq
	}

}
