package peterlavalle.puregen

import javax.sound.sampled.{AudioInputStream, AudioSystem, Clip, DataLine}
import marytts.LocalMaryInterface
import marytts.exceptions.SynthesisException

object MaryTalk {

	/**
	 * very slow function - speaks something regardless of how long it takes to say
	 *
	 * ... so ... "printf()" but for TTS
	 */
	def apply(): String => Unit = {
		val localMaryInterface: LocalMaryInterface = new LocalMaryInterface()
		(text: String) =>
			if (text.trim.nonEmpty)
				try {
					localMaryInterface.generateAudio(text).using {
						audioInputStream: AudioInputStream =>

							val info = new DataLine.Info(classOf[Clip], audioInputStream.getFormat)
							AudioSystem.getLine(info).asInstanceOf[Clip].using {
								clip: Clip =>
									clip.open(audioInputStream)
									clip.start()

									val microsecondLength: Long = clip.getMicrosecondLength

									Thread.sleep(microsecondLength / 1000L)

									while (clip.isRunning)
										Thread.sleep(10)

									clip.close()
									audioInputStream.close()
							}
					}
				} catch {
					case s: SynthesisException =>
						throw new Exception(s"mary didnt't like `$text`", s)
				}
	}
}
