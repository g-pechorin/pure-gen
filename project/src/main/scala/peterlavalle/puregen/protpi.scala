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
				case IR.Bool => "Bool"

				case IR.Real32 => "Real32"
				case IR.Real64 => "Real64"

				case IR.SInt32 => "SInt32"
				case IR.SInt64 => "SInt64"

				case IR.Text => "String"

				case IR.ListOf(kind) => "Array[" + kind.toScala + "]"

				case IR.Opaque(name) => name
				case IR.Struct(name, _) => name

				case _: IR.Import =>
					error("you can't create a scala type for an import (but you tried)")
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
