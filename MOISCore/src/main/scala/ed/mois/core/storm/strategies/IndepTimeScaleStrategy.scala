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
    val tCurr = Array.fill(processes.length)(0.0)
    val tPrev = Array.fill(processes.length)(0.0)
    val tNext = Array.fill(processes.length)(dt)
    val pDt = Array.fill(processes.length)(dt)
    // Initialize result map
    var m = collection.mutable.Map.empty[Double, model.StateType]
    if (debug) println(printStatesHeader(model.stateVector, 6))

    var changes = List.empty[StormChange]
    
    // Step through whole simulation
    var t = 0.0
    var stopper = 0
    // Add to result vector
    m += (t -> model.stateVector.dupl)
    if (debug) println(printStates(t, model.stateVector, 6))

    while (t < maxTime) {
      stopper += 1
      // if (stopper > 982){
      // println("tNext: " + tNext.mkString("[ ", ", ", " ]"))
      // println("tCurr: " + tCurr.mkString("[ ", ", ", " ]"))
      // println("tPrev: " + tPrev.mkString("[ ", ", ", " ]"))
      // println("pDt:   " +   pDt.mkString("[ ", ", ", " ]"))}
      // Calculate internal model dependencies
      model.calcDependencies(model.stateVector)

      // Get index of minimal next time step
      val i = tNext.zipWithIndex.minBy(_._1)._2
      //println(s"Calculated i: $i")
      // Let this one process produce its change
      val change = processes(i)._evolve(m(tCurr(i)).dupl, tCurr(i), pDt(i))
      // Zip all the results and check for violations
      changes = change ::: changes
      val dupl = m(tCurr(i)).dupl
      val violators = intersect(dupl, dupl.fieldPtrs, changes, t, pDt(i))
      if (violators.isDefined) {
        // Adjust current process' time step (up to next violation point / 2.0)
        pDt(i) = (violators.get._1 + (violators.get._2 - violators.get._1) / 2.0 - tCurr(i)) // 2.0
        tNext(i) = tCurr(i) + pDt(i)
        // Adjust all other processes' time step. Get ids first
        val violProcIds = violators.get._3.map(_.origin).distinct.filter(_ != i)
        println("Violating! Mainly " + violProcIds.mkString("(", ", ", ")") + " plus i=" + i)
        // And adjust all dt's, tCurr's, ...
        violProcIds.foreach { pid => 
          pDt(pid) = tCurr(i) - tPrev(pid) + pDt(i)
          tCurr(pid) = tPrev(pid)
          tNext(pid) = tPrev(pid) + pDt(pid)
        }
        // Remove all changes of violating processes
        changes = changes.filterNot(c => violProcIds.contains(c.origin) | c.origin == i)
      } else {
        // If there were no violations, integrate all newly found changes and overwrite state history
        //m.foreach(x => println(printStates(x._1, x._2, 6)))
        //println(s"Trying to merge: ${tCurr(i)} -> ${tNext(i) - tCurr(i)}")
        m = merge(m, changes, tCurr(i), tNext(i) - tCurr(i))
        // Adjust i's times and the global time
        // Try to put pDt(i) back to original dt
        pDt(i) = math.min(pDt(i) * 2.0, dt)
        tPrev(i) = tCurr(i)
        tCurr(i) = tNext(i)
        tNext(i) = tCurr(i) + pDt(i)
        // Add to result vector
        //m += (t -> model.stateVector.dupl)
        //if (debug) println(printStates(t, model.stateVector, 6))
        t = tCurr.min
        changes = changes.filter(c => c.tEnd > t)
      }
      //println(s"keys at $t are: " + m.keys.toList.sorted.mkString(", "))
      //m.foreach(x => println(printStates(x._1, x._2, 6)))
      //println(stopper + ": " + t + ": " + i)
      if (stopper > 10000) t = maxTime
      //println
    }

    def merge(m: collection.mutable.Map[Double, model.StateType], changes: List[StormChange], startTime: Double, dt: Double) = {
      val mm = m.filter{case (t, s) => t <= startTime}
      val dupl = mm(startTime).dupl      
      //println(s"keys of mm before are: " + mm.keys.toList.sorted.mkString(", "))
      intersectAndStore(mm, dupl, dupl.fieldPtrs, changes, startTime, dt)
      //println(s"keys of mm after are: " + mm.keys.toList.sorted.mkString(", "))
      mm.filter{case (t, s) => t >= startTime}.foreach{case (t, s) => model.calcDependencies(s)}
      mm
    }
    
    TreeMap(m.toMap.toArray:_*)
  }
}