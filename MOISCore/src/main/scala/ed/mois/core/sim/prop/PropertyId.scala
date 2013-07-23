/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.prop

object PropertyId {
  def decompose(fullName: String) = {
    val split = fullName.split('.') 
    (split(0), split(1))
  }
  def compose(state: String, property: String) = state + "." + property
}

case class PropertyId(state: String, name: String, var id: Int) {
  def this(fullName: String, id: Int) {
    this(PropertyId.decompose(fullName)._1, PropertyId.decompose(fullName)._2, id)
  }
  
  def fullName = PropertyId.compose(state, name)
}