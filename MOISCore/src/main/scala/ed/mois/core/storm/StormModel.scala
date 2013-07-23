/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

/**
 * Defines a model that can be simulated.
 */
abstract class StormModel {
  type StateType <: StormState[StateType]

  val title: String
  val desc: String
  val authors: String
  val contributors: String

  val stateVector: StateType
  val processes: Array[() => StormProcess[StateType]]

  val observables = List.empty[StormField[_]]

  def calcDependencies(st: StateType)
}