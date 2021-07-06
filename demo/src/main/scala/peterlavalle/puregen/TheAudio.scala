package peterlavalle.puregen

import java.io.InputStream

import S3.Audio
import peterlavalle.{Channel, daemon}

object TheAudio {

	implicit class PiInputStream4(stream: InputStream) {
		def capacitor(len: Int)(into: Array[Byte] => Unit): AutoCloseable = {

			val bytes: Array[Byte] = Array.ofDim[Byte](len)

			daemon.reader(stream.read(bytes))((_: Int) != -1) {
				read: Int =>
					assume(0 <= read)
					if (0 != read)
						into(bytes.clone().take(read))
			}
		}
	}

	class MikeAudio(micro: Micro) extends Audio.AudioLine {
		val channel: Channel[Array[Byte], Array[Byte]] = {
			val channel: Channel[Array[Byte], Array[Byte]] =
				Channel[Array[Byte]]()

			micro.audioInputStream
				.capacitor(16000 * 2)(channel.post)

			micro.start()

			channel
		}
	}

}

trait TheAudio extends Audio.D {
	val line = new TheAudio.MikeAudio(Micro())

	override protected def S3_Audio_openMicrophone(): () => Audio.AudioLine = () => line
}
