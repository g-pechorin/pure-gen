package peterlavalle.puregen

import S3.GCASR
import com.google.cloud.speech.v1
import com.google.cloud.speech.v1.{SpeechRecognitionAlternative, SpeechRecognitionResult}
import com.google.protobuf.Duration
import peterlavalle.Channel

trait TheGCASR extends GCASR.D {

	import GCASR._
	import S3.Audio.AudioLine

	implicit class ExtDuration(duration: Duration) {
		def toDoubleSeconds: Double =
			duration.getSeconds * 1.0 + (duration.getNanos * 0.000000001)

	}

	override protected def S3_GCASR_openGoogleASR(send: GoogleASR.Trigger): GoogleASR.Signal => Unit =
		new GoogleASR.Receiver {


			val wSon: TWSon.I =
				WSonGoogleASR.Transcription()
					.bind {
						speechRecognitionResult: SpeechRecognitionResult =>
							val alternatives: Stream[Alternative] = speechRecognitionResult.getAlternativesList
								.toStream
								.map {
									alternative: SpeechRecognitionAlternative =>
										Alternative(
											alternative.getConfidence,
											alternative.getTranscript,
											alternative.getWordsList.toStream
												.map {
													wordInfo: v1.WordInfo =>
														WordInfo(
															wordInfo.getStartTime.toDoubleSeconds,
															wordInfo.getEndTime.toDoubleSeconds,
															wordInfo.getWord
														)
												}
										)
								}
							send.GRecognised(
								alternatives.head.transcript,
								alternatives
							)
					}
					.open((_: Unit) => ())

			var last: Channel[Array[Byte], Array[Byte]] = null

			override def GConnect(a0: AudioLine): Unit =
				a0 match {
					case mikeAudio: TheAudio.MikeAudio =>
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
