/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies

import akka.actor._
import scala.collection.immutable.TreeMap
import scala.concurrent._
import ExecutionContext.Implicits.global

import ed.mois.core.storm._
import ed.mois.core.storm.comm._

/**
 * Simulation strategies define how the simulator behaves.
 * They operate on a set of states (that contain the initial state values)
 * and a set of processes that each define an evolve method which can
 * be called on arbitrary states / times and delta times.
 */
abstract class SimulationStrategy extends Actor {
  // Debug variable that can be overwritten and allows debugging output
  val debug = false

  /**
   * Main routine that simulates an environment.
   *
   * @param states Array of states.
   * @param processes Array of processes that define evolve methods.
   * @return The final simulation state.
   */
  def simulate(model: StormModel): TreeMap[Double, StormState[_]]

  def receive = {
    case RunSimulation(model) => {
      sender ! simulate(model)
      context.stop(self)
    }
  }
}