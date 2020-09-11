package peterlavalle.puregen

object protpi {

	implicit class PiListKind(action: IR.TAction) {
		def take: String =
			action.args.zipWithIndex.map {
				case (k, i) =>
					"a" + i + ": " + k.toScala
			}.foldLeft("")(_ + ", " + _).drop(2)

		def pass: String =
			action.args.indices.map("a" + _)
				.foldLeft("")(_ + ", " + _).drop(2)

		def push: String =
			action.args.indices.map("a" + _ + ".asInstanceOf[Object]")
				.foldLeft("")(_ + ", " + _).drop(2)

	}

	implicit class PiTFSF(fsf: IR.TFSF[_]) {
		/**
		 * is the signal function "simpe" or ... do we need a type
		 */
		def isSimple: Boolean =
			List("=" -> 1) == fsf.actions.map { case (a: IR.TAction) => a.name -> a.args.size }.toList
	}

	implicit class PiTKind(ir: IR.TKind) {
		def toScala: String =
			ir match {
				case IR.Text => "String"
				case IR.SInt32 => "Int"
				case IR.Real32 => "Float"
				case IR.Real64 => "Double"

				case IR.Opaque(name) => name
			}
	}

	implicit class PiArgs(args: List[IR.TKind]) {
		def toArgs: String =
			args match {
				case Nil => ""
				case _ =>
					args
						.map((_: IR.TKind).toScala)
						.zipWithIndex
						.map {
							case (kind, index) =>
								s"a$index: $kind"
						}
						.reduce((_: String) + ", " + (_: String))
			}
	}

}
