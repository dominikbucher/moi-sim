/*
 * Contains tiny process.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny

/**
 * A tiny process. Needs a name and an evolve function.
 */
abstract class TinyProcess[T] {
  def name: String
  def evolve(states: T, t: Double, dt: Double): T
}