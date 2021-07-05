package peterlavalle.puregen

import S3.Sphinx
import edu.cmu.sphinx.api.SpeechResult
import edu.cmu.sphinx.result.WordResult
import peterlavalle.Channel

trait TrySphinx extends Sphinx.D {

	import S3.Audio.AudioLine
	import Sphinx._

	override protected def S3_Sphinx_openCMUSphinx4ASR(log: Boolean, send: CMUSphinx4ASR.Trigger): CMUSphinx4ASR.Signal => Unit = {
		new CMUSphinx4ASR.Receiver {

			val wSon: TWSon.I =
				WSonSphinx4
					.bind {
						(speechResult: SpeechResult) =>

							val recognised =
								CMUSphinx4ASR.SRecognised(

									speechResult.getHypothesis,

									Result(
										hypothesis = speechResult.getHypothesis,

										bestFinalResultNoFiller = speechResult.getResult.getBestFinalResultNoFiller,
										bestPronunciationResult = speechResult.getResult.getBestPronunciationResult,
										bestResultNoFiller = speechResult.getResult.getBestResultNoFiller
									),

									speechResult.getWords.toStream
										.map {
											word: WordResult =>
												WordInfo(
													confidence = word.getConfidence,
													score = word.getScore,
													start = word.getTimeFrame.getStart * 0.001,
													end = word.getTimeFrame.getEnd * 0.001,
													filler = word.getWord.isFiller,
													spelling = word.getWord.getSpelling,
												)
										}
								)

							if (log) {
								println("speechResult.getHypothesis = " + recognised.a0)
							}

							recognised

					}
					.open(send(_: CMUSphinx4ASR.SRecognised))

			var last: Channel[Array[Byte], Array[Byte]] = null

			override def SConnect(a0: AudioLine): Unit =
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

			override def SDisconnect(): Unit =
				if (null != last) {
					last.rem(wSon)
					last = null
				}
		}
	}
}
