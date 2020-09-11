package peterlavalle

import java.io.{InputStream, OutputStream}

trait PiOutputStreamT {

	implicit class PiOutputStream[O <: OutputStream](outputStream: O) {

		final def <<(from: InputStream): O = {

			val data: Array[Byte] = Array.ofDim[Byte](128)

			@scala.annotation.tailrec
			def recu(read: Int): O =
				read match {
					case -1 =>
						from.close()
						outputStream

					case _ =>
						outputStream.write(data, 0, read)
						recu(from read data)
				}

			recu(from read data)
		}
	}

}
