package peterlavalle.puregen

import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

import S3.Sphinx
import com.google.cloud.speech.v1
import com.google.cloud.speech.v1.{SpeechRecognitionAlternative, SpeechRecognitionResult}
import edu.cmu.sphinx.api.SpeechResult
import edu.cmu.sphinx.result.WordResult
import peterlavalle.Channel

trait TrySphinx extends Sphinx.D {

	import Sphinx._


	val line = new MikeAudio(Micro())

	override protected def S3_Sphinx_openMicrophone(): () => AudioLine = {
		() => line
	}

	override protected def S3_Sphinx_openCMUSphinx4ASR(send: CMUSphinx4ASR.Trigger): CMUSphinx4ASR.Signal => Unit = {
		new CMUSphinx4ASR.Receiver {

			val wSon: TWSon.I =
				WSonSphinx4
					.bind {
						(speechResult: SpeechResult) =>

							CMUSphinx4ASR.SRecognised(

								speechResult.getHypothesis,

								SphinxResult(
									hypothesis = speechResult.getHypothesis,

									bestFinalResultNoFiller = speechResult.getResult.getBestFinalResultNoFiller,
									bestPronunciationResult = speechResult.getResult.getBestPronunciationResult,
									bestResultNoFiller = speechResult.getResult.getBestResultNoFiller
								),

								speechResult.getWords.toStream
									.map {
										word: WordResult =>
											SphinxWord(
												confidence = word.getConfidence,
												score = word.getScore,
												start = word.getTimeFrame.getStart * 0.001,
												end = word.getTimeFrame.getEnd * 0.001,
												filler = word.getWord.isFiller,
												spelling = word.getWord.getSpelling,
											)
									}
							)
					}
					.open(send(_: CMUSphinx4ASR.SRecognised))

			var last: Channel[Array[Byte], Array[Byte]] = null

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

	override protected def S3_Sphinx_openGoogleASR(send: GoogleASR.Trigger): GoogleASR.Signal => Unit =

		new GoogleASR.Receiver {


			val wSon: TWSon.I =
				WSonGoogleASR.Transcription()
					.bind {
						speechRecognitionResult: SpeechRecognitionResult =>


							val alternatives: Stream[Alternative] = speechRecognitionResult.getAlternativesList
								.toStream
								.map {
									alternative: SpeechRecognitionAlternative =>
										alternative.getWordsList

										Alternative(
											alternative.getConfidence,
											alternative.getTranscript,
											alternative.getWordsList.toStream
												.map {
													wordInfo: v1.WordInfo =>
														WordInfo(
															wordInfo.getStartTime.getSeconds * 1.0 + (wordInfo.getStartTime.getNanos * 0.000000001),
															wordInfo.getEndTime.getSeconds * 1.0 + (wordInfo.getEndTime.getNanos * 0.000000001),
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
					//						.bind(send.GRecognised)
					.open((_: Unit) => ())

			var last: Channel[Array[Byte], Array[Byte]] = null

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

	class MikeAudio(micro: Micro) extends Sphinx.AudioLine {
		val channel: Channel[Array[Byte], Array[Byte]] = {
			val channel: Channel[Array[Byte], Array[Byte]] =
				Channel[Array[Byte]]()

			micro.audioInputStream
				.capacitor(16000 * 2)(channel.post)

			micro.start()

			channel
		}
	}

	implicit class PiInputStream4(stream: InputStream) {
		def capacitor(len: Int)(into: Array[Byte] => Unit): AutoCloseable =
			new AutoCloseable {

				val live = new AtomicBoolean(true)

				val work: AutoCloseable =
					daemon {
						val bytes: Array[Byte] = Array.ofDim[Byte](len)

						loop(stream.read(bytes))(_ != -1 && live.get()) {
							read: Int =>
								assume(0 <= read)
								if (0 != read)
									into(bytes.clone().take(read))
						}
					}


				override def close(): Unit = {
					live.set(false)
					work.close()
				}
			}
	}
}
