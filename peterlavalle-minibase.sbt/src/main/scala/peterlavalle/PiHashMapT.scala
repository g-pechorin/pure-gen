package peterlavalle

import java.util

trait PiHashMapT {

	implicit class PiHashMap[K, V](map: util.HashMap[K, V]) {
		def opt(key: K)(value: => V): V =
			if (map containsKey key)
				map get key
			else {
				map put(key, value)
				map get key
			}
	}

}
