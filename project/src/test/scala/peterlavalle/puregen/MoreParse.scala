package peterlavalle.puregen

class MoreParse extends TParseTest {
	override def module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Opaque("Data"),
				IR.Pipe("Sound",
					List(),
					Set(
						IR.ActionGet("Listen", List(IR.Opaque("Data"))),
						IR.ActionSet("Sense", List(IR.Real32)),
					)
				)
			)
		)
}
