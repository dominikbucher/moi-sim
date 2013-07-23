/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.comm

import akka.actor.ActorRef
import ed.mois.core.storm._

sealed trait Message

/** Starts simulation. */
case class RunSimulation(model: StormModel) extends Message

/** Evolves system based on a process.evolve method. */
case class Evolve[T <: StormState[_]](state: T, t: Double, dt: Double) extends Message
/** Results from a process.evolve method. */
case class Result[T <: StormState[_]](state: T, t: Double, dt: Double) extends Message