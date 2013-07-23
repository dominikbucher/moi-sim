/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.chg

import ed.mois.core.math.Mat
import ed.mois.core.sim.prop.PropertyId
import ed.mois.core.sim.prop.TrackedProperty

/**
 * Trait that allows definition of tracked properties and stores all changes to those.
 * New tracked properties are defined via [[trackable[T]()]].
 */
trait TrackChange {
  /**
   * A list of property changes that will be reset after a call to [[resetChanges]].
   */
  protected var propertyChanges = List[PropertyChange]()

  /**
   * Collects all current changes performed on tracked properties.
   *
   * @return The list of property changes.
   */
  def collectChanges: List[PropertyChange] = propertyChanges.reverse

  /**
   * Resets all recorded changes. Does not rollback properties on the state!
   */
  def resetChanges = propertyChanges = List[PropertyChange]()

  /**
   * Method that is called whenever a property changes its value.
   *
   * TODO Think about a more general way, maybe something like track[T](...); Would need adapted [[Change]] class too.
   *
   * @param prop The property that changes.
   * @param before Initial value.
   * @param after Value afterwards.
   */
  def track[T](prop: PropertyId, from: T, to: T) = {
    (from, to) match {
      case (f: Int, t: Int) => {
        propertyChanges = IntChange(prop, t - f) :: propertyChanges
      }
      case (f: Double, t: Double) => {
        propertyChanges = DoubleChange(prop, t - f) :: propertyChanges
      }
      case (f: Boolean, t: Boolean) => {
        propertyChanges = BooleanChange(prop, t) :: propertyChanges
      }
      case (f: Char, t: Char) => {
        propertyChanges = CharChange(prop, t) :: propertyChanges
      }
      case (f: List[_], t: List[_]) => {
        println(s"Tracking list change: $prop. This is NOT implemented yet.")
      }
      case (f: String, t: String) => {
        println(s"Tracking string change: $prop. This is NOT implemented yet.")
      }
      case (f: Map[_, _], t: Map[_, _]) => {
        println(s"Tracking map change: $prop. This is NOT implemented yet.")
      }
      case (f: Array[Int], t: Array[Int]) => {
        println(s"Tracking array[Int] change: $prop. This is NOT implemented yet.")
      }
      case (f: Array[Double], t: Array[Double]) => {
        println(s"Tracking array[Double] change: $prop. This is NOT implemented yet.")
      }
      case (f: Mat, t: Mat) => {
        propertyChanges = MatChange(prop, t - f) :: propertyChanges
      }
      case (_, _) => println(s"Trying to track random objects: $prop. This wont work, sorry.")
    }
    //println(s"tracking property $prop: $before -> $after")
  }

  /**
   * Defines a new property that is tracked. Needs an initial value and a name.
   *
   * @param init The initial value.
   * @param name The name of the property (best practice use variable name).
   * @return The trackable property.
   */
  def trackable[T](init: T, id: PropertyId) = new TrackedProperty[T](init, id, this)
}

