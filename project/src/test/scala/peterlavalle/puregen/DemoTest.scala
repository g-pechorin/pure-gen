package peterlavalle.puregen

/**
 * test what I'm doing in the demo
 *
 * ... as of the point where i wrote this
 */
class DemoTest extends
	TParseTest {

	import IR.Pi._

	override val module: peterlavalle.puregen.IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Sample("Age", Nil, IR.Real32),
				IR.Signal("Kick", Nil, IR.Real32),
				IR.Signal("Status", IR.Text, IR.Text),
				IR.Event("Eve", Nil, IR.Text),
			)
		)
}
