/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

import ed.mois.core.util.Refl

/**
 * The storm state defines the complete state vector of the simulation.
 * It assigns unique id's to all the fields. Those id's are used all over
 * the simulation to store and send variables around. 
 *
 * Create a case class (or any other class that implements a copy() method)
 * to use the StormState trait. 
 */
trait StormState[T <: StormState[T]] {
  // Self has to implement a copy method (this just duplicates all the basic values,
  // fields are copied within the dupl() method in this trait)
  self: {def copy(): T} =>
  /**
   * The fields map that maps the field values to their respective field id's.
   */
  var fields = collection.mutable.Map.empty[Int, Any]
  /**
   * The fields pointers that map the fields to their id's.
   */
  var fieldPtrs = collection.mutable.Map.empty[Int, StormField[_]]
  /**
   * Field names generated from the variable names.
   */
  lazy val fieldNames = Refl.getStormCCFieldNames(this)

  /**
   * Initializes a new field.
   */
  def field[A](init: A) = {
    new StormField(init, fields.size, fields, fieldPtrs)
  }
  
  /**
   * Duplicates this state by calling copy of the superclass and copying all
   * field values to the new state. This is NOT a deep copy, i.e. if you 
   * reference objects in fields, they will point to the same object. 
   *
   * WARNING: This is an issue for distributed simulators.
   */
  def dupl: T = {
    val n = this.copy
    for (f <- n.fields) {
      n.fields(f._1) = fields(f._1)
    }
    n
  }
  
  /**
   * Prints the state to console.
   */
  def print {
    println(toLongString)
  }
  
  /**
   * Creates a string containing all fields.
   */
  def toLongString: String = {
    fieldNames.map{ case (i, s) => s + ":" + fields(i)}.mkString(", ")
  }
}