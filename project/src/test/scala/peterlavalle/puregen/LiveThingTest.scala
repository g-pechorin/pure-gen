package peterlavalle.puregen

class LiveThingTest extends
	TParseTest {

	override def module: peterlavalle.puregen.IR.Module = {
		val utterance: IR.Struct = IR.Struct("Utterance", List(("start", IR.Real32), ("words", IR.Text)))
		IR.Module(
			getClass.getSimpleName,
			Set(
				utterance,
				IR.Pipe("LiveMary",
					List(IR.Text),
					Set(
						IR.ActionSet("Silent", List()), IR.ActionSet("Speak", List(utterance)),
						IR.ActionGet("Speaking", List(IR.Real32, IR.Text)), IR.ActionGet("Spoken", List(IR.Real32, IR.Text))
					)
				)
			)
		)
	}
}
