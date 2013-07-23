/*
 * Contains tiny fields.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny

/**
 * A property of type [[T]].
 *
 * @param <T> Type of the property.
 * @param init The initial value.
 * @param name The name of the property.
 */
 class Field[T](init: T, val id: Int,
  val fields: collection.mutable.Map[Int, Any], val fieldPtrs: collection.mutable.Map[Int, Field[_]]) {
  // Set field initial value
  fields += id -> init
  fieldPtrs += id -> this

  /** Retrieve the value held in this property.   */
  def apply(): T = fields(id).asInstanceOf[T]

  /** Update the value held in this property, through the setter. */
  def update(newValue: T) = fields(id) = newValue

  def reset = { fields(id) = init }

  override def toString = fields(id).toString

  var geq: Option[T] = None
  def >=(t: T) = {
    geq = Some(t)
    this
  }

  var leq: Option[T] = None
  def <=(t: T) = {
    leq = Some(t)
    this
  }
}