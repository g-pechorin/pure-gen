package peterlavalle.puregen

import com.google.cloud.speech.v1.SpeechRecognitionResult
import edu.cmu.sphinx.api.SpeechResult
import pdemo.Sphinx
import peterlavalle.puregen.TModule.Sample
import peterlavalle.{Channel, TLogs}

class TrySphinx() extends Sphinx with TLogs {

	override def openMicrophone(): Sample[AudioLine] = {

		val line = new MikeAudio(Micro())

		sample {
			line
		}
	}

	override def openCMUSphinx4ASR(): TModule.Pipe[CMUSphinx4ASR.Ev, CMUSphinx4ASR.Si] =
		pipe {
			send: CMUSphinx4ASR.Ev =>

				val wSon: TWSon.I =
					WSonSphinx4
						.bind((_: SpeechResult).getHypothesis)
						.open(send.SRecognised)

				var last: Channel[Array[Byte], Array[Byte]] = null

				// send a nothing to "kick" the thing
				send.SRecognised("")

				new CMUSphinx4ASR.Si {
					override def SConnect(a0: AudioLine): Unit =
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

					override def SDisconnect(): Unit =
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

	override def openGoogleASR(): TModule.Pipe[GoogleASR.Ev, GoogleASR.Si] =
		pipe {
			send: GoogleASR.Ev =>

				val wSon: TWSon.I =
					WSonGoogleASR.Transcription()
						.bind((_: SpeechRecognitionResult).getAlternatives(0).getTranscript).bind(send.GRecognised)
						.open((_: Unit) => ())

				var last: Channel[Array[Byte], Array[Byte]] = null

				// send a nothing to "kick" the thing
				send.GRecognised("")

				new GoogleASR.Si {
					override def GConnect(a0: AudioLine): Unit =
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

					override def GDisconnect(): Unit =
						if (null != last) {
							last.rem(wSon)
							last = null
						}
				}
		}
}
