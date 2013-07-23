/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

/**
 * A storm process. Needs a name and an evolve function.
 */
abstract class StormProcess[T] {
  def name: String
  def _evolve(states: T, t: Double, dt: Double): T = {
  	evolve(states, t, dt)
  	states
  }
  def evolve(states: T, t: Double, dt: Double)
}