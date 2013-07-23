/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.state

import ed.mois.macr.ChangeModelMacros
import scala.language.experimental.macros

/**
 * Defines a trait that can be mixed into classes that depend on state skeletons / properties.
 * It allows to create "shadow" copies of states which are automatically tracked and updated
 * over the distributed structure.
 */
trait StateDependency {
  /**
   * Collection of states used in this class. Add new ones by calling [[state[T]()]].
   */
  var states = collection.mutable.Map.empty[String, StateSkeleton]

  /**
   * Adds a state to the class. The state will be automatically updated to
   * contain the most recent state information.
   *
   * The way this is implemented is so that an instance of the given [[StateSkeleton]] is created
   * that can be used locally as if it were the state object of the simulation.
   *
   * This function creates (via macros) code as follows:
   * = {
   * 	val v = {final class $anon extends T { val name = stateName }; new $anon()};
   * 	states += (stateName -> v);
   *  	v
   *   }
   * Meaning that a new object is created, added to the states variable and then returned. The
   * whole thing is a convenience method so the programmer of the process doesn't have to think
   * about implementing a concrete instance of the skeleton.
   *
   * @param stateName The name of the state as it appears in the simulation.
   * @return The newly created property.
   */
  def state[T <: StateSkeleton](stateName: String): T = macro ChangeModelMacros.instantiateSkeleton_impl[T]
}