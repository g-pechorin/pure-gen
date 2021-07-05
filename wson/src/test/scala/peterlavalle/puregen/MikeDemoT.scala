package peterlavalle.puregen

import java.io.{File, FileInputStream, FileOutputStream, InputStream}

import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem, Clip}
import peterlavalle.{Step, StepSide}

trait MikeDemoT {

	lazy val slots: Int = 14


	lazy val storage: File = new File("target") / "samples"

	lazy val samples: Int = 16000

	lazy val slotFiles = {
		def slotNames: Stream[String] = {
			(0 until slots).toStream
				.map("sample-%03d".format(_: Int))
		}

		slotNames.map {
			name: String =>
				(storage / name)
		}
	}

	def main(args: Array[String]): Unit = {

		collect(args)

		replay(args)


		compute(args) {
			text: String =>
				println(text)
		}.using {
			worker: TWSon.I =>

				okayLoop("running" -> "press okay to send // cancel to quit") {
					slotFiles
						.map(new FileInputStream(_: File))
						.foreach(worker send (_: FileInputStream))
				}
		}

	}

	def collect(args: Array[String]): Unit = {

		okayLoop("capture audio" -> "press okay to (re)capture audio") {

			val mike: Micro = Micro()


			class AnyMore(private var live: Boolean = true) extends (() => Boolean) with Runnable with AutoCloseable {
				override def run(): Unit = synchronized {
					require(!live)
					live = true
					notifyAll()
				}

				override def apply(): Boolean = synchronized(live)

				override def close(): Unit = synchronized {
					require(live)
					live = false
					notifyAll()
				}
			}

			val more = new AnyMore()

			val step: Step[Array[Byte], Unit] = {


				val files =
					slotFiles
						.iterator

				StepSide {
					data: Array[Byte] =>
						if (files.hasNext)
							new FileOutputStream(files.next().EnsureParent)
								.using((_: FileOutputStream).write(data))
						else {
							if (more()) {
								more.close()
								println("all samples collected")
								require(!more())
							}
						}
				}
			}

			val iterator: Iterator[Array[Byte]] = {

				mike.start()

				Stream
					.continually {
						if (!more())
							null
						else
							mike.audioInputStream
								.readNBytes((2 * samples))
					}
					.takeWhile(null != _).iterator
			}
			val what: AutoCloseable =
				more && step ! iterator && mike

			okayLoop("yeah" -> "what") {
				println("hmm ...")
			}

			TODO("fix this so there's a proper close and no need for a prompt")
			more.close()

			what.close()

		}
	}

	def replay(args: Array[String]): Unit = {

		def reOpen(sampleRate: Float = 16000, sampleSize: Int = 16, signed: Boolean = true, bigEndian: Boolean = false): InputStream => AudioInputStream = {

			import javax.sound.sampled.AudioInputStream

			val format = new AudioFormat(sampleRate, sampleSize, 1, signed, bigEndian)

			(data: InputStream) =>
				new AudioInputStream(data, format, samples)
		}

		okayLoop("play audio" -> "press okay to hear audio") {


			val clip: Clip = AudioSystem.getClip

			slotFiles.foreach {
				file: File =>

					val stream: AudioInputStream = reOpen()(new FileInputStream(file))

					clip.open(stream)
					clip.start()

					val slice_pause: Int = 10
					Thread.sleep(slice_pause)

					while (clip.isRunning)
						Thread.sleep(slice_pause)

					clip.stop()
					clip.close()
			}
		}
	}

	def compute(args: Array[String])(found: String => Unit): TWSon.I =
		new TWSon.I {
			override def close(): Unit = {}
		}
}
