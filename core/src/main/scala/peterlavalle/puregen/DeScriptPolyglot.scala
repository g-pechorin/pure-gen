package peterlavalle.puregen

import org.graalvm.polyglot.Value
import peterlavalle.De

object DeScriptPolyglot extends De[Value] {
	implicit val deScriptDouble: DeScript[Double] = (_: Value).asDouble()
	implicit val deScriptString: DeScript[String] = (_: Value).asString()
	implicit val deScriptInt: DeScript[Int] = (_: Value).asInt()
	implicit val deScriptBoolean: DeScript[Boolean] = (_: Value).asBoolean()
	implicit val deScriptLong: DeScript[Long] = (_: Value).asLong()
	implicit val deScriptFloat: DeScript[Float] = (_: Value).asFloat()
	implicit val deScriptByte: DeScript[Byte] = (_: Value).asByte()
	implicit val deScriptShort: DeScript[Short] = (_: Value).asShort()

	override val get: (Value, String) => Value = (_: Value) getMember (_: String)
	override val seq: Value => Iterable[Value] = {
		value: Value =>
			require(value.hasArrayElements)
			value.as[Array[Value]](classOf[Array[Value]])
	}
}
