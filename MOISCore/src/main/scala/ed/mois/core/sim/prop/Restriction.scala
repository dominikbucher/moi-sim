/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.prop

import scala.math.Numeric

abstract class Restriction {
  def restriction(p: Any): Boolean
}

case class GreaterEquals(a: Double) extends Restriction {
  def restriction(p: Any): Boolean = {
    p match {
      case pp: Double => pp > a
      case pp: Int => pp > a
      case _ => true
    }
  }
}