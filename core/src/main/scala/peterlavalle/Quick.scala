package peterlavalle

import java.util.Random

trait Quick[T] {
	def generate(random: Random): T
}

object Quick {


	implicit class PriJRandom(random: Random) {
		def nextQuick[T](implicit quick: Quick[T]): T = quick.generate(random)

		def limited[T](bound: Int)(implicit quick: Quick[T]): Stream[T] =
			streamOf.take(random.nextInt(bound))

		def streamOf[T](implicit quick: Quick[T]): Stream[T] = {
			val subRandom: Random = random.subRandom
			Stream.continually(subRandom.nextLong())
				.map(new Random(_: Long))
				.map(quick.generate(_: Random))
		}

		def subRandom: Random = new Random(random.nextLong())

		def randomString(
											min: Int = 18,
											max: Int = 37,
											chars: Set[Char] = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toSet
										): String = {
			new String(
				(0 until (min + random.nextInt(max - min)))
					.map {
						_: Int =>
							chars.toSeq(random.nextInt(chars.size))
					}
					.toArray
			)
		}

		def choose[O](options: (() => O)*): O =
			options(random.nextInt(options.length))()
	}

}
