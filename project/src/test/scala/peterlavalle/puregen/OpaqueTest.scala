package peterlavalle.puregen

class OpaqueTest extends
	TParseTest {

	import IR.Pi._

	override def module: peterlavalle.puregen.IR.Module = {
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Opaque("Audio"),
				IR.Event("Mike", List(),
					Set(
						IR.ActionGet("=", IR.Opaque("Audio")),
					)
				)
			)
		)
	}
}

