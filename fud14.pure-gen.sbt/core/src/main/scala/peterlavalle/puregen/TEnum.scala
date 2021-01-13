package peterlavalle.puregen

import org.graalvm.polyglot.Value

/**
 * used for implicits with the data-enums
 *
 * if the generics emit implementation we can ad-hoc the usage
 */
@deprecated()
trait TEnum[T] {
	@deprecated()
	def link(news: Array[Value], anyRef: E): Value

	@deprecated()
	def read(send: E => Unit): T

	@deprecated()
	def send(call: E, into: T): Unit

	@deprecated()
	trait E

}
