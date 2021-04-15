package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

class S3Import extends AnyFunSuite
	with TGenS3Test

	with TParseTest {

	import IR.Pi._

	override val includeModule: String => IR.Module = {
		case "Bar" => IR.Module("Bar", Set(IR.Opaque("Foo")))
	}

	override def module: IR.Module = {
		val foo: IR.Import = IR.Import("Foo", includeModule("Bar"))
		IR.Module(
			getClass.getSimpleName,
			Set(
				foo,
				IR.Sample("AudioFeed", Nil, foo),
				IR.Signal("LogOut", IR.Text, foo),
			)
		)
	}
}
