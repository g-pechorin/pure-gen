package peterlavalle.puregen

import S3.Mary

trait TryMary extends Mary.D {

	import Mary._

	override protected def S3_Mary_openLiveMary(split: String, send: LiveMary.Trigger): LiveMary.Signal => Unit = {
		val live: MaryLive =
			if ("" == split)
				MaryLive.kirk()
			else
				MaryLive.kirk(split)

		val ifNewer: Newer[Double] = new Newer(-1.0)

		import LiveMary._
		{
			case Silent() =>
				ifNewer() {
					live.speak("", () => {}, (_: Boolean) => {})
				}
			case Speak(a0: Utterance) =>
				ifNewer(a0.start) {
					live.speak(a0.words, () => send.Speaking(a0), (full: Boolean) => if (full) send.Spoken(a0))
				}
		}
	}

	class Newer[D <: Double](i: D) {
		private var l: D = i

		def apply(v: D)(o: => Unit): Unit =
			synchronized {
				require(i <= v)
				if (v > l) {
					l = v
					o
				}
			}

		def apply()(o: => Unit): Unit =
			synchronized {
				l = i
				o
			}
	}

}
