/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies

import scala.collection.immutable.TreeMap

import ed.mois.core.storm._
import ed.mois.core.util.Refl
import ed.mois.core.storm.comm._

/**
 * Strategy that uses Karr's random sequential simulation strategy.
 */
class KarrStrategy(maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper with ChangeHelper {

  def simulate(model: StormModel): TreeMap[Double, StormState[_]] = {
    // Create processes (model.processes provides creator functions)
    val processes = model.processes.map(p => p()).toList
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

      // Randomly go through processes
      for {p <- util.Random.shuffle(processes)
           c <- p._evolve(model.stateVector.dupl, time, dt)} {
        c.chg.foreach(v => model.stateVector.fieldPtrs(v._1).merge(v._2, c.dt, dt))
      }
    }
    
    TreeMap(m.toMap.toArray:_*)
  }
}