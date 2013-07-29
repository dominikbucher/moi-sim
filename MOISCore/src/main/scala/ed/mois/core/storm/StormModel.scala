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
  var processes = Array.empty[() => StormProcess[StateType]]

  /**
   * Adds a process to this model. The wrapper is necessary, as this allows some 
   * logic to be wrapped around the instantiation. 
   */
  def addProcess(creator: () => StormProcess[StateType]) = {
  	val pId = processes.length
  	val f: () => StormProcess[StateType] = {
  		() => {
  			val p = creator()
  			p.id = pId
  			p
  		}
  	}
  	processes = processes :+ f
  	this
  }

  /**
   * Shortcut to add process.
   */
  def ++(creator: () => StormProcess[StateType]) = addProcess(creator)

  /**
   * List of observables, is graphed right after simulation stops.
   */
  val observables = List.empty[StormField[_]]

  /**
   * Calculates inter-state dependencies. 
   */
  def calcDependencies(st: StateType)
}