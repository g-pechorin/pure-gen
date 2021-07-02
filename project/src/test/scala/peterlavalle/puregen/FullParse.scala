package peterlavalle.puregen

import peterlavalle.puregen.IR.Pi._

class FullParse extends TParseTest {


	override def module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Signal("Foo", List(IR.Text),
					Set(
						IR.ActionSet("Trip", List()),
						IR.ActionSet("Tame", List(IR.SInt32)),
					)
				),
				IR.Event("Bar", List(IR.Text),
					Set(
						IR.ActionGet("Touch", List())
					)
				),
				IR.Event("Flap", IR.Text, IR.Text),
				IR.Sample("Ship", IR.SInt32, IR.Real32),
				IR.Signal("Vampire", Nil, IR.Real64),
				IR.Opaque("Reap"),

				IR.Sample("Strip", List(IR.Real64),
					Set(
						IR.ActionGet("Flip", List(IR.Text)),
						IR.ActionGet("Tape", List(IR.Opaque("Reap")))
					)
				),


				IR.Opaque("Data"),
				IR.Pipe("Sound",
					List(),
					Set(
						IR.ActionSet("Sense", List(IR.Real32)),
						IR.ActionGet("Listen", List(IR.Opaque("Data"))),
					)
				),

			)
		)
}
