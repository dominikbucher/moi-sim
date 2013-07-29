/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

/**
 * A storm process. Needs a name and an evolve function.
 */
trait StormProcess[T <: StormState[_]] {
  /**
   * The name of this process. 
   */
  def name: String

  /**
   * Id of this process. 
   */
  var id: Int = 0

  /**
   * Internal evolve method, wraps the user defined one by doing some
   * preliminary tasks. 
   */
  def _evolve(states: T, t: Double, dt: Double): List[StormChange] = {
  	states.resetChanges
  	evolve(states, t, dt)
  	List(StormChange(id, t, dt, states.collectChanges))
  }

  /**
   * User-specified evolve method that does this process' magic. 
   */
  def evolve(states: T, t: Double, dt: Double)
}