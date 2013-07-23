/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.prop

import ed.mois.core.sim.chg._

/**
 * A tracked property that monitors all changes to itself done via update.
 * Use by calling [[propertyName() = newValue]].
 *
 * @param <T> The property type.
 * @param init The initial value.
 * @param name Name of the property.
 * @param tracker Instance that tracks changes to this property.
 */
class TrackedProperty[T](init: T, id: PropertyId, tracker: TrackChange) extends Property(init, id) {
  /**
   * Updates the property with a new value. Calls [[track]] within the tracker object.
   *
   * @param newValue
   */
  override def update(newValue: T) = {
    val oldValue = value
    value = setter(newValue)
    tracker.track[T](id, oldValue, value)
  }

  /**
   * Checks if this property gets violated when applying a list of changes.
   *
   * @param pcs List of changes to apply to this property.
   * @return A tuple with a boolean (if a violation happened) and a double (the scaling factor
   * of all involved changes).
   */
  def violate_?(pcs: List[Tuple2[Change, PropertyChange]]): Tuple2[Boolean, Double] = {
    // If there is no restriction on the property, it can't be violated
    // TODO Booleans, Strings, ... can ALWAYS be violated (not additive)
    return (false, 1.0)
    //    if (restricted) {
    //      pcs.head._2 match {
    //        case DoubleChange(name, value) => {
    //          // Add all changes
    //          val newVal = this.value.asInstanceOf[Double] + pcs.foldLeft(0.0)((left, pc) => pc._1.nTimes * pc._2.asInstanceOf[DoubleChange].value + left)
    //          // Look if there is a violation
    //          val violate = (newVal) < 0
    //          // Calculate scaling factor
    //          (violate, (this.value.asInstanceOf[Double] - newVal) / this.value.asInstanceOf[Double])
    //        }
    //        case IntChange(name, value) => {
    //          val newVal = this.value.asInstanceOf[Int] + pcs.foldLeft(0.0)((left, pc) => pc._1.nTimes * pc._2.asInstanceOf[IntChange].value + left)
    //          val violate = (newVal) < 0
    //          (violate, (this.value.asInstanceOf[Int] - newVal) / this.value.asInstanceOf[Double])
    //        }
    //        case BooleanChange(name, value) => {
    //          (pcs.length > 1, 1.0)
    //        }
    //        case c @ MatChange(name, value) => {
    //          val newVal = this.value.asInstanceOf[Mat] + pcs.foldLeft(new Mat(c.value.nRows, c.value.nCols))((left, pc) => pc._2.asInstanceOf[MatChange].value @* pc._1.nTimes + left)
    //          val violate = !newVal.isPositive
    //          if (violate) {
    //            val mostNegative = newVal.getMostNegative
    //            (violate, (this.value.asInstanceOf[Mat](mostNegative._2, mostNegative._3) - mostNegative._1) / this.value.asInstanceOf[Mat](mostNegative._2, mostNegative._3))
    //          } else {
    //            (violate, 1.0)
    //          }
    //        }
    //      }
    //    } else (false, 1.0)
  }
}