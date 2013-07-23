/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.chg

import ed.mois.core.math.Mat
import ed.mois.core.sim.prop.PropertyId

/**
 * A BasicChange modifies one iderty of a [[State]].
 */
abstract class PropertyChange(val id: PropertyId, val value: Any)
case class DoubleChange(override val id: PropertyId, override val value: Double) extends PropertyChange(id, value)
case class IntChange(override val id: PropertyId, override val value: Int) extends PropertyChange(id, value)
case class BooleanChange(override val id: PropertyId, override val value: Boolean) extends PropertyChange(id, value)
case class CharChange(override val id: PropertyId, override val value: Char) extends PropertyChange(id, value)
case class ListChange(override val id: PropertyId, override val value: List[Any]) extends PropertyChange(id, value)
case class MapChange(override val id: PropertyId, override val value: Map[Any, Any]) extends PropertyChange(id, value)
case class StringChange(override val id: PropertyId, override val value: String) extends PropertyChange(id, value)
case class MatChange(override val id: PropertyId, override val value: Mat) extends PropertyChange(id, value)
case class ArrayChange(override val id: PropertyId, override val value: Array[Any]) extends PropertyChange(id, value)

