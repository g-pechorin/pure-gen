package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

class PipeArg extends AnyFunSuite
	with TGenS3Test

	with TParseTest {

	import IR.Pi._

	override def module: IR.Module = {
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Pipe(
					"Foo",
					List(IR.Text),
					Set(
						IR.ActionGet("Status", IR.Bool),
						IR.ActionSet("Action", IR.Real64),
					)
				),
			)
		)
	}
}
