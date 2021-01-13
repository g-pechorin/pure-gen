package peterlavalle.puregen

import java.io.File

import peterlavalle.{Err, TemplateResource}

import scala.io.{BufferedSource, Source}

object PureIn extends TemplateResource {

	private val ext = ".pidl"

	def parse(src: Seq[File]): Seq[IR.Module] = {
		src
			.flatMap {
				src: File =>
					(src ** ((_: String).endsWith(ext)))
						.sorted
						.map(src -> (_: String))
			}
			.distinctBy((_: (File, String))._2)
			.map {
				case (root, name) =>
					Source.fromFile(root / name).using {
						src: BufferedSource =>
							assume(name.endsWith(ext))

							PCG(name.dropRight(ext.length).replace('/', '.'), src.mkString) match {
								case Err(v) =>
									v
							}
					}
			}
	}

}
