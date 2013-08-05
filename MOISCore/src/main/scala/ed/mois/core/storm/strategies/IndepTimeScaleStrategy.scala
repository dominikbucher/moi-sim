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
 * Strategy that uses independent time scales.
 */
class IndepTimeScaleStrategy(maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper with ChangeHelper {

  def simulate(model: StormModel): TreeMap[Double, StormState[_]] = {
    // Create processes (model.processes provides creator functions)
    val processes = model.processes.map(p => p())
    val tNext = Array.fill(processes.length)(0.0)
    val tCurr = Array.fill(processes.length)(0.0)
    val tPrev = Array.fill(processes.length)(dt)
    val pDt = Array.fill(processes.length)(dt)
    // Initialize result map
    var m = collection.mutable.Map.empty[Double, model.StateType]
    if (debug) println(printStatesHeader(model.stateVector, 6))

    var changes = List.empty[StormChange]
    
    // Step through whole simulation
    var t = 0.0
    while (t < maxTime) {
      // Calculate internal model dependencies
      model.calcDependencies(model.stateVector)
      // Add to result vector
      m += (t -> model.stateVector.dupl)
      if (debug) println(printStates(t.toDouble * dt, model.stateVector, 6))

      // Get index of minimal next time step
      val i = tNext.zipWithIndex.minBy(_._1)._2
      // Let this one process produce its change
      val change = processes(i)._evolve(m(tCurr(i)).dupl, t, pDt(i))
      // Zip all the results and check for violations
      changes = change ::: changes
      val violators = intersect(model.stateVector, model.stateVector.fieldPtrs, changes, t, pDt(i))
      if (violators.isDefined) {
        // Adjust current process' time step (up to next violation point / 2.0)
        pDt(i) = (violators.get._1 - tCurr(i)) / 2.0
        // Adjust all other processes' time step. Get ids first
        val violProcIds = violators.get._3.map(_.origin).distinct.filter(_ != i)
        // And adjust all dt's, tCurr's, ...
        violProcIds.foreach { pid => 
          pDt(pid) = tCurr(i) - tPrev(pid) + pDt(i)
          tCurr(pid) = tPrev(pid)
          tNext(pid) = tPrev(pid) + pDt(pid)
        }
      } else {
        // If there were no violations, integrate all newly found changes and overwrite state history
        m = merge(m, changes, t, pDt(i))
        // Adjust i's times and the global time
        tPrev(i) = tCurr(i)
        tCurr(i) = tNext(i)
        tNext(i) = tCurr(i) + pDt(i)
        t = tCurr.min
        changes = changes.filter(c => c.tEnd > t)
      }
    }

    def merge(m: collection.mutable.Map[Double, model.StateType], changes: List[StormChange], startTime: Double, dt: Double) = {
      m.filterKeys(t => t <= startTime)
      val dupl = m(startTime).dupl
      intersect(dupl, dupl.fieldPtrs, changes, startTime, dt)
      m
    }
    
    TreeMap(m.toMap.toArray:_*)
  }
}