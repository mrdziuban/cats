package cats
package kernel
package instances

import scala.collection.immutable.{IndexedSeq => ImIndexedSeq}
import scala.collection.mutable
import compat.scalaVersionSpecific._

@suppressUnusedImportWarningForScalaVersionSpecific
object StaticMethods extends cats.kernel.compat.HashCompat {

  def wrapMutableMap[K, V](m: mutable.Map[K, V]): Map[K, V] =
    new WrappedMutableMap(m)

  private[kernel] class WrappedMutableMap[K, V](m: mutable.Map[K, V])
      extends kernel.compat.WrappedMutableMapBase[K, V](m) {
    override def size: Int = m.size
    def get(k: K): Option[V] = m.get(k)
    def iterator: Iterator[(K, V)] = m.iterator
  }

  /**
   * When you "own" this m, and will not mutate it again, this
   * is safe to call. It is unsafe to call this, then mutate
   * the original collection.
   *
   * You are giving up ownership when calling this method
   */
  def wrapMutableIndexedSeq[A](m: mutable.IndexedSeq[A]): ImIndexedSeq[A] =
    new WrappedIndexedSeq(m)

  private[kernel] class WrappedIndexedSeq[A](m: mutable.IndexedSeq[A]) extends ImIndexedSeq[A] {
    override def length: Int = m.length
    override def apply(i: Int): A = m(i)
    override def iterator: Iterator[A] = m.iterator
  }

  def iteratorCompare[A](xs: Iterator[A], ys: Iterator[A])(implicit ev: Order[A]): Int = {
    while (true) {
      if (xs.hasNext) {
        if (ys.hasNext) {
          val x = xs.next()
          val y = ys.next()
          val cmp = ev.compare(x, y)
          if (cmp != 0) return cmp
        } else {
          return 1
        }
      } else {
        return if (ys.hasNext) -1 else 0
      }
    }
    0
  }

  def iteratorPartialCompare[A](xs: Iterator[A], ys: Iterator[A])(implicit ev: PartialOrder[A]): Double = {
    while (true) {
      if (xs.hasNext) {
        if (ys.hasNext) {
          val x = xs.next()
          val y = ys.next()
          val cmp = ev.partialCompare(x, y)
          if (cmp != 0.0) return cmp
        } else {
          return 1.0
        }
      } else {
        return if (ys.hasNext) -1.0 else 0.0
      }
    }
    0.0
  }

  def iteratorEq[A](xs: Iterator[A], ys: Iterator[A])(implicit ev: Eq[A]): Boolean = {
    while (true) {
      if (xs.hasNext) {
        if (ys.hasNext) {
          if (ev.neqv(xs.next(), ys.next())) return false
        } else {
          return false
        }
      } else {
        return !ys.hasNext
      }
    }
    true
  }

  def combineNIterable[A, R](b: mutable.Builder[A, R], x: Iterable[A], n: Int): R = {
    var i = n
    while (i > 0) { b ++= x; i -= 1 }
    b.result()
  }

  def combineAllIterable[A, R](b: mutable.Builder[A, R], xs: IterableOnce[Iterable[A]]): R = {
    xs.iterator.foreach(b ++= _)
    b.result()
  }

  // Adapted from scala.util.hashing.MurmurHash#productHash.
  def product1Hash(_1Hash: Int): Int = {
    import scala.util.hashing.MurmurHash3._
    var h = productSeed
    h = mix(h, _1Hash)
    finalizeHash(h, 1)
  }

  // Adapted from scala.util.hashing.MurmurHash#productHash.
  def product2Hash(_1Hash: Int, _2Hash: Int): Int = {
    import scala.util.hashing.MurmurHash3._
    var h = productSeed
    h = mix(h, _1Hash)
    h = mix(h, _2Hash)
    finalizeHash(h, 2)
  }
}
