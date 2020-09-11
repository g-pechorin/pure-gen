package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

class TakePass extends AnyFunSuite {

	_root_.peterlavalle.TODO("do a test of the/a DSL when/if soon")

	if (false) {
		trait Take[T]

		trait Taker[T] {
			def map(f: Unit => T): Take[T]
		}

		// pass an atomic value in
		def take[T <: AnyVal]: Taker[T] =
			???

		def q: Take[Int] =
			for {
				_ <- take
			} yield {
				8
			}

		// pass a string in

		def text: Taker[String] =
			???

		val w: Take[String] =
			for {
				_ <- text
			} yield {
				""
			}

		// post some other value

		trait Pass[T, O] {
			def flatMap(f: T => Take[O]): Take[O]
		}

		def pass[T, O](f: T): Pass[T, O] =
			???

		// ask for another value

		trait Want[T, O] {
			def flatMap(f: T => Take[O]): Take[O]
		}

		def want[T, O](f: T => Boolean): Want[T, O] =
			???

		def s: Take[Int] =
			for {
				q <- pass("sss")
				_ <- take
			} yield {
				8
			}

		trait Data

		trait Send[T <: Data]

		trait Sender[T <: Data] {
			def map(f: T => Unit): Send[T]
		}

		def send[T <: Data]: Send[T] =
			???

	}
}
