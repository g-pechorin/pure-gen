package peterlavalle.puregen

import com.google.cloud.speech.v1.SpeechRecognitionResult
import pdemo.Sphinx
import peterlavalle.puregen.TModule.{Event, Sample}
import peterlavalle.{Channel, TLogs}

class TrySphinx() extends Sphinx with TLogs {

	override def openLiveSphinx(): Event[String] = {
		for {
			run <- trigger[String]
		} {

			error("don't do this no more")

		}
	}

	override def openMicrophone(): Sample[AudioLine] = {

		val line = new MikeAudio(Micro())

		sample {
			line
		}
	}

	override def openFullSphinx(): TModule.Pipe[FullSphinx.Ev, FullSphinx.Si] =
		pipe {
			send: FullSphinx.Ev =>

				val wSon: TWSon.I =
				//					WSonSphinx4
				//						.bind((_: SpeechResult).getHypothesis)
				//						.open(send.Recognised)
					WSonGoogleASR.Transcription()
						.bind((_: SpeechRecognitionResult).getAlternatives(0).getTranscript).bind(send.Recognised)
						.open((_: Unit) => ())

				var last: Channel[Array[Byte], Array[Byte]] = null

				// send a nothing to "kick" the thing
				send.Recognised("")

				new FullSphinx.Si {
					override def Connect(a0: AudioLine): Unit =
						a0 match {
							case mikeAudio: MikeAudio =>
								if (last != mikeAudio.channel) {
									if (null != last)
										last.rem(wSon)

									last = mikeAudio.channel

									last.sub(wSon) {
										data: Array[Byte] =>
											wSon.send(data)

									}
								}
						}

					override def Disconnect(): Unit =
						if (null != last) {
							last.rem(wSon)
							last = null
						}
				}
		}

	class MikeAudio(micro: Micro) extends AudioLine {
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
