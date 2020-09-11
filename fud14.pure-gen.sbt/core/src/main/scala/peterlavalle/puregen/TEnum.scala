package peterlavalle.puregen

import org.graalvm.polyglot.Value

/**
 * used for implicits with the data-enums
 *
 * if the generics emit implementation we can ad-hoc the usage
 */
trait TEnum[T] {

	def link(news: Array[Value], anyRef: E): Value

	def read(send: E => Unit): T

	def send(call: E, into: T): Unit

	trait E

}
