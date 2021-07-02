package peterlavalle.puregen

object S3IndexClasses extends S3 {
	override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] =
		Map(
			pack.replace('.', '/') + "/S3.scala" -> {

				bind("txt") {
					case "pack" => pack
					case "with" =>
						modules.map(_.name).toList.sorted

					case "hard" =>
						modules.map(pack + '_' + _.name).toList.sorted.map(_.replace('.', '_'))

					case "vals" =>
						modules.toStream.map((_: IR.Module).name).sorted.map {
							name: String =>
								"\tval val_" + pack.replace('.', '_') + "_" + name.replace('.', '_') + ": " + pack + "." + name
						}
				}.force
			}
		)
}


