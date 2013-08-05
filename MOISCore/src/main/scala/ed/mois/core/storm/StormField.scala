/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

/**
 * A field of type [[T]].
 *
 * @param <T> Type of the field.
 * @param init The initial value.
 * @param id The id of the property.
 * @param fields Reference to map where this fields value will be added.
 * @param fieldPtrs Reference to map where this field will be added.
 */
 class StormField[T](state: StormState[_],
  init: T, val id: Int,
  val fields: collection.mutable.Map[Int, Any], 
  val fieldPtrs: collection.mutable.Map[Int, StormField[_]]) {

  var dirty = false

  // Set field initial value
  fields += id -> init
  fieldPtrs += id -> this

  /** Retrieve the value held in this property.   */
  def apply(): T = fields(id).asInstanceOf[T]

  /** Update the value held in this property, through the setter. */
  def update(newValue: T) = {
    init match {
      case d: Double => {
        val prevChg = if (state.changes.contains(id)) state.changes(id).asInstanceOf[Double] else 0.0
        state.changes(id) = prevChg + newValue.asInstanceOf[Double] - fields(id).asInstanceOf[Double]
      }
      case a: Any => {
        state.changes(id) = newValue
      }
    }
    fields(id) = newValue
  }

  def reset = { fields(id) = init }

  override def toString = fields(id).toString

  def merge(chg: Any, chgUnit: Double, dt: Double): Boolean = {
    init match {
      case d: Double => {
        fields(id) = fields(id).asInstanceOf[Double] + chg.asInstanceOf[Double] * dt / chgUnit
        if (geq.isDefined && geq.get.asInstanceOf[Double] > fields(id).asInstanceOf[Double]) return false
        if (leq.isDefined && leq.get.asInstanceOf[Double] < fields(id).asInstanceOf[Double]) return false
        return true
      }
      case a: Any => {
        fields(id) = chg
        if (dirty) return false
        else dirty = true
        return true
      }
    }
    true
  }

  /**
   * Greater or equals than restriction.
   */
  var geq: Option[T] = None
  def >=(t: T) = {
    geq = Some(t)
    this
  }

  /**
   * Lesser or equals than restriction.
   */
  var leq: Option[T] = None
  def <=(t: T) = {
    leq = Some(t)
    this
  }
}