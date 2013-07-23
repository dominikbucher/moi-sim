/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies

import java.text.DecimalFormat

import ed.mois.core.storm._

/**
 * Defines some helper functions which are useful for simulation strategies.
 */
trait StrategyHelper {
  /**
   * Prints state names in a nice way (formatted to contain maximal maxChars
   * characters).
   *
   * @param states The states to print.
   * @param maxChars Maximal number of characters to print.
   * @return The formatted state names.
   */
  def printStatesHeader(states: StormState[_], maxChars: Int): String = {
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
  def printStates(time: Double, states: StormState[_], maxChars: Int): String = {
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
  def printResults(time: Double, pNames: Array[String], results: Array[StormState[_]], processes: Array[StormProcess[_ <: StormState[_]]]): String = {
    results.zipWithIndex.filter(t => pNames.contains(processes(t._2).name)).map {
      case (r, i) => processes(i).name + ": " + printStates(time, r, 6)
    }.mkString(", ")
  }
}