/*
 * Contains general simulation strategy outline.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny.strategies

import ed.mois.core.tiny._
import scala.util.Random
import ed.mois.core.util.Refl
import java.text.DecimalFormat

/**
 * Simulation strategies define how the simulator behaves.
 * They operate on a set of states (that contain the initial state values)
 * and a set of processes that each define an evolve method which can
 * be called on arbitrary states / times and delta times.
 */
abstract class SimulationStrategy[T <: StateVector[T]] {
  /**
   * Main routine that simulates an environment.
   *
   * @param states Array of states.
   * @param processes Array of processes that define evolve methods.
   * @return The final simulation state.
   */
  def simulate(states: T, processes: Array[TinyProcess[T]], tiny: Tiny[T]): Map[Double, T]

  /**
   * Prints state names in a nice way (formatted to contain maximal maxChars
   * characters).
   *
   * @param states The states to print.
   * @param maxChars Maximal number of characters to print.
   * @return The formatted state names.
   */
  def printStatesHeader(states: T, maxChars: Int): String = {
    val stateStr = states.fields.map { s =>
      val str = states.fieldNames(s._1).toString
      str.substring(0, math.min(str.length(), maxChars))
    }
    "  \t" + stateStr.mkString("\t")
  }
  
  /**
   * Prints states in a nice way (formatted to contain maximal maxChars
   * characters).
   *
   * @param time The time when printStates is called.
   * @param states The states to print.
   * @param maxChars Maximal number of characters to print.
   * @return The formatted states.
   */
  def printStates(time: Double, states: T, maxChars: Int): String = {
    val stateStr = states.fields.map { s =>
      val str = s._2.toString
      str.substring(0, math.min(str.length(), maxChars))
    }
    "%.1f".format(time) + "\t" + stateStr.mkString("\t")
  }

  /**
   * Prints results of processes in a nice way.
   *
   * @param time Time when printResults is called.
   * @param pNames Process names of processes that should be printed.
   * @param results The results vector.
   * @param processes The processes.
   * @return The formatted results.
   */
  def printResults(time: Double, pNames: Array[String], results: Array[T], processes: Array[TinyProcess[T]]): String = {
    results.zipWithIndex.filter(t => pNames.contains(processes(t._2).name)).map {
      case (r, i) => processes(i).name + ": " + printStates(time, r, 6)
    }.mkString(", ")
  }
}