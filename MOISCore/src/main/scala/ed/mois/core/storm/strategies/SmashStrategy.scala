/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies

import akka.actor._
import scala.util.Random
import scala.concurrent._
import scala.collection.immutable.TreeMap
import ExecutionContext.Implicits.global

import ed.mois.core.storm._
import ed.mois.core.util.Refl

/**
 * Strategy that mashes everything by adding additive resources and
 * randomly choosing between processes otherwise. There is no notion of
 * connected changes in this case, each change on a variable is to be
 * seen as completely independent (as in contrast to connected changes
 * where A only happens if B is also admitted).
 *
 * @param maxTime Maximal sim-time the simulation runs for.
 * @param dt Stepsize of algorithm.
 */
class SmashStrategy(maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper {
  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());

  def simulate(model: StormModel): TreeMap[Double, StormState[_]] = {
    // Create processes (model.processes provides creator functions)
    val processes = model.processes.map(p => p())
    // Initialize result map
    val m = collection.mutable.Map.empty[Double, StormState[_]]
    if (debug) println(printStatesHeader(model.stateVector, 6))
    
    // Step through whole simulation
    for (t <- 0 to (maxTime / dt).toInt) {
      val time = t.toDouble * dt
      // Calculate internal model dependencies
      model.calcDependencies(model.stateVector)
      // Add to result vector
      m += (time -> model.stateVector.dupl)
      if (debug) println(printStates(t.toDouble * dt, model.stateVector, 6))
      // Step forward in time
      val results = processes.map(_._evolve(model.stateVector.dupl, time, dt))
      // Merge all fields
      for (f <- model.stateVector.fields) {
        model.stateVector.fields(f._1) = mash(f._2, results.map(_.fields(f._1)).toArray)
      }
    }
    
    TreeMap(m.toMap.toArray:_*)
  }

  /**
   * Mash supports different basic types. Doubles are handled additively
   * (without any constraints though).
   *
   * @param init The initial value.
   * @param newS What processes would like to change it to.
   * @return A new value for init (if anyone wanted to change it).
   */
  def mash(init: Any, newS: Array[Any]): Any = {
    init match {
      case d: Double => newS.foldLeft(d)((b, t) => b + (t.asInstanceOf[Double] - d))
      case o => {
        val chgs = newS.filter(!_.equals(o))
        if (chgs.length > 1) {
          val ri = rand.nextInt(chgs.length); chgs(ri)
        } else if (chgs.length == 1) {
          chgs(0)
        } else {
          o
        }
      }
    }
  }
}