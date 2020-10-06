package peterlavalle.puregen

import pdemo.Mary
import peterlavalle.puregen.TModule.Pipe
import peterlavalle.puregen.util.Newer

class TryMary() extends Mary {

	override def openLiveMary(split: String): Pipe[LiveMary.Ev, LiveMary.Si] = {
		pipe {
			e: LiveMary.Ev =>

				val live: MaryLive =
				//MaryLive.log("full-mary")
					if ("" == split)
						MaryLive.kirk()
					else
						MaryLive.kirk(split)

				val ifNewer: Newer[Float] = Newer(-1.0f)

				new LiveMary.Si {
					override def Silent(): Unit =
						ifNewer() {
							live.speak("", () => {}, (_: Boolean) => {})
						}

					override def Speak(a0: Float, a1: String): Unit =
						ifNewer(a0) {
							live.speak(a1, () => e.Speaking(a0, a1), (full: Boolean) => if (full) e.Spoken(a0, a1))
						}
				}
		}
	}
}
