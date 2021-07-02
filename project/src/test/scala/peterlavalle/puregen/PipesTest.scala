package peterlavalle.puregen

/**
 * test what I'm doing in the demo
 *
 * ... as of the point where i wrote this
 */
class PipesTest extends
	TParseTest {

	import IR.Pi._

	override def module: peterlavalle.puregen.IR.Module = {
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Opaque("Audio"),
				IR.Pipe("TTS", List(),
					Set(
						IR.ActionSet("Silent", List(IR.Real32)),
						IR.ActionSet("Speak", List(IR.Real32, IR.Text)),
						IR.ActionGet("Speaking", List(IR.Real32, IR.Text)),
						IR.ActionGet("Spoken", List(IR.Real32, IR.Text)),
						IR.ActionGet("Silence", IR.Real32),
					)
				)
			)
		)
	}
}
