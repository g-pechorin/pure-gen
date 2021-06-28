package peterlavalle.puregen

import peterlavalle.puregen.IR.Pi._

class TestParse extends TParseTest {
	override def module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Signal("Foo", IR.Text,
					Set(
						IR.ActionSet("Trip", Nil),
						IR.ActionSet("Tame", IR.SInt32),
					)
				),
				IR.Event("Bar", IR.Text, Set(IR.ActionGet("Touch", Nil))),
				IR.Sample("Strip", IR.Real64,
					Set(
						IR.ActionGet("Flip", IR.Text)
					)
				)
			)
		)
}
