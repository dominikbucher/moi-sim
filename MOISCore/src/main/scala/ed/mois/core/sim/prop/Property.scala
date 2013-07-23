/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.prop

import ed.mois.core.math.Mat
import ed.mois.core.sim.chg._

/**
 * A property of type [[T]].
 *
 * @param <T> Type of the property.
 * @param init The initial value.
 * @param name The name of the property.
 */
class Property[T](init: T, val id: PropertyId) {
  /**
   * Restrictions define criteria why a change of the property could be rejected.
   * At the moment this is a boolean saying that the property cannot go below zero.
   */
  var restrictions = List.empty[Restriction]

  /**
   * Defines this property to always stay positive.
   *
   * @return This property for method chaining.
   */
  def positive_! = {
    withRestriction(GreaterEquals(0.0))
  }

  /**
   * Defines this property to always stay positive.
   *
   * @return This property for method chaining.
   */
  def withRestriction(rest: Restriction) = {
    restrictions = rest :: restrictions
    this
  }

  def checkRestrictions: Boolean = {
    restrictions.forall(_.restriction(this()))
  }

  /**
   * The value of this property.
   */
  protected var value: T = init

  /** The getter function, defaults to identity. */
  protected var setter: T => T = identity[T]

  /** The setter function, defaults to identity. */
  protected var getter: T => T = identity[T]

  /** Retrieve the value held in this property.   */
  def apply(): T = getter(value)

  /** Update the value held in this property, through the setter. */
  def update(newValue: T) = value = setter(newValue)

  def checkChanges(chgs: List[PropertyChange]) = {

  }

  def applyChanges(chgs: List[PropertyChange], deltaT: Double): Unit = {
    chgs.foreach(applyChange(_, deltaT))
  }

  /**
   * Applies a [[PropertyChange]] exactly one time.
   *
   * @param chg The change to apply.
   */
  def applyChange(chg: PropertyChange): Unit = {
    applyChange(chg, 1)
  }

  /**
   * Applies a [[PropertyChange]] several times.
   *
   * @param chg The change to apply.
   * @param nTimes How many times the change should be applied.
   */
  def applyChange(chg: PropertyChange, nTimes: Double) = {
    chg match {
      case IntChange(name, newVal) =>
        value = setter((value.asInstanceOf[Int] + nTimes * newVal).toInt.asInstanceOf[T])
      case DoubleChange(name, newVal) =>
        value = setter((value.asInstanceOf[Double] + nTimes * newVal).asInstanceOf[T])
      case BooleanChange(name, newVal) =>
        value = setter(newVal.asInstanceOf[T])
      case MatChange(name, newVal) =>
        value = setter((value.asInstanceOf[Mat] + newVal @* nTimes.toDouble).asInstanceOf[T])
      case _ =>

    }
  }

  /** Change the getter. */
  protected def get(newGetter: T => T) = { getter = newGetter; this }

  /** Change the setter */
  protected def set(newSetter: T => T) = { setter = newSetter; this }

  def copy = {
    val p = new Property(value, id)
    p.restrictions = restrictions
    p
  }

  def reset = { value = init }

  //override def toString() = s"${id.fullName} (= $value; init was $init)"
  override def toString() = s"${id.fullName} = $value"
}