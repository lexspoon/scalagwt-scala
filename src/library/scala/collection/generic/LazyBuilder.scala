/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: ListBuffer.scala 14378 2008-03-13 11:39:05Z dragos $

package scala.collection.generic

// import immutable.{List, Nil, ::}
import mutable.ListBuffer

/** A builder that constructs its result lazily. Iterators or iterables to 
 *  be added to this builder with `++=` are not evaluated until `result` is called.
 */
abstract class LazyBuilder[Elem, +To] extends Builder[Elem, To] {
  /** The different segments of elements to be added to the builder, represented as iterators */
  protected var parts = new ListBuffer[Traversable[Elem]]
  def +=(x: Elem): this.type = { parts += List(x); this }
  override def ++=(xs: Iterator[Elem]): this.type = { parts += xs.toStream; this }
  override def ++=(xs: Traversable[Elem]): this.type = { parts += xs; this }
  def result(): To
  def clear() { parts.clear() } 
}