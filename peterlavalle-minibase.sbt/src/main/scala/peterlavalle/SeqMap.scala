package peterlavalle

case class SeqMap[K, V](data: List[(K, V)] = Nil) {
	def apply(k: K): V = find(k).get._2

	def find(k: K): Option[(K, V)] = data.find((_: (K, V))._1 == k)

	def contains(k: K): Boolean = find(k).nonEmpty

	def add(kv: (K, V)): SeqMap[K, V] = SeqMap(kv :: data)
}
