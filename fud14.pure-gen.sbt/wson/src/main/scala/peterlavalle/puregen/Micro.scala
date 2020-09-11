package peterlavalle.puregen

import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem, TargetDataLine}
import peterlavalle.CanStart


trait Micro extends CanStart with AutoCloseable {
	val audioInputStream: AudioInputStream
}

object Micro {
	def apply(sampleRate: Float = 16000, sampleSize: Int = 16, signed: Boolean = true, bigEndian: Boolean = false): Micro = {

		import javax.sound.sampled.AudioInputStream

		val format = new AudioFormat(sampleRate, sampleSize, 1, signed, bigEndian)

		val line: TargetDataLine =
			AudioSystem.getTargetDataLine(format)

		new Micro {
			override val audioInputStream: AudioInputStream = new AudioInputStream(line)

			override def close(): Unit = {
				line.stop()
				line.close()
			}

			override def start(): Unit = {
				line.open()
				line.start()
			}
		}
	}
}
