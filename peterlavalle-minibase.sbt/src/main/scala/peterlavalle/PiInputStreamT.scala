package peterlavalle

import java.io.InputStream

import scala.annotation.tailrec

trait PiInputStreamT {

	implicit class PiInputStream(inputStream: InputStream) {


		@deprecated(
			"use readAllBytes() introduced in java 9",
			"java 9 / 2020-08-27"
		)
		def toArray: Array[Byte] = inputStream.readAllBytes()

		def fill(buffer: Array[Byte]): Array[Byte] = {

			@tailrec
			def loop(read: Int): Array[Byte] = {
				if (read == buffer.length)
					buffer
				else {
					require(read < buffer.length && read >= 0)
					val more: Int = inputStream.read(buffer, read, buffer.length - read)
					require(0 <= more)
					loop(
						more + read
					)
				}
			}

			loop(0)
		}
	}

}
