package peterlavalle.puregen

class DataParse extends TParseTest with TGenScalaTest {
	override val module =

		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Opaque("Data")
			)

		)

	override def srcScala: String =
		"""
			|import org.graalvm.polyglot.{HostAccess, Value}
			|import peterlavalle.puregen.{TEnum, TModule}
			|import scala.reflect.ClassTag
			|import peterlavalle._
			|
			|package foo.bar {
			|	trait DataParse extends TModule {
			|		trait Data extends Opaque
			|	}
			|}
			|
  	""".stripMargin
}
