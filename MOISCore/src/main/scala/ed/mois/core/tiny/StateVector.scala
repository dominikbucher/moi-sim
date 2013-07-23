/*
 * Contains tiny state vector.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny

import ed.mois.core.util.Refl

trait StateVector[T <: StateVector[T]] {
  self: {def copy(): T} =>
  var fields = collection.mutable.Map.empty[Int, Any]
  var fieldPtrs = collection.mutable.Map.empty[Int, Field[_]]
  lazy val fieldNames = Refl.getTinyCCFieldNames(this)

  def field[T](init: T) = {
    new Field(init, fields.size, fields, fieldPtrs)
  }
  
  def dupl: T = {
    val n = this.copy
    for (f <- n.fields) {
      n.fields(f._1) = fields(f._1)
    }
    n
  }
  
  def print {
    println(toLongString)
  }
  
  def toLongString: String = {
    fieldNames.map{ case (i, s) => s + ":" + fields(i)}.mkString(", ")
  }
}