/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.util.Random
import scala.concurrent._
import scala.concurrent.duration._

import scala.collection.immutable.TreeMap

import ed.mois.core.storm._
import ed.mois.core.util.Refl
import ed.mois.core.storm.comm._

/**
 * Strategy that uses independent time scales.
 */
class DistrSimPosStepAdaptionStrategy(val model: StormModel, maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper with ChangeHelper {
  // Dispatcher for dealing with futures
  import context.dispatcher
  // Implicit timeout for dealing with actors and waiting for their results
  implicit val timeout = Timeout(60 seconds)

  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());

  case class ChangeResults(pid: Int, chgs: List[StormChange])

  // Create processes (model.processes provides creator functions)
  val processes: Map[Int, ActorRef] = model.processes.zipWithIndex.map(p => p._2 -> context.actorOf(Props(() => new StormDistrProcess(p._1())))).toMap
  // Create arrays containing different process' data
  val tCurr = Array.fill(processes.size)(0.0)
  val tPrev = Array.fill(processes.size)(0.0)
  val tNext = Array.fill(processes.size)(dt)
  val pDt = Array.fill(processes.size)(dt)

  // Set of running processes, and where in time they are
  var running = collection.mutable.Set.empty[Tuple3[Int, Double, Double]]
  // Initialize result map
  var m = collection.mutable.Map.empty[Double, model.StateType]
  // Enter first value
  m += (0.0 -> model.stateVector.dupl)
  // Print header in console
  if (debug) println(printStatesHeader(model.stateVector, 6))

  // Smallest consistent state time
  var t = 0.0
  // Bunch of active changes
  var changes = List.empty[StormChange]

  // Originating actorRef, will get results sent back
  var orig: Option[ActorRef] = None

  // Overrides the default receive method to receive messages from processes
  override def receive = {
    // Run simulation message that just dispatches to simulate
    case RunSimulation(model) => {
      orig = Some(sender)
      simulate(model)
    }
    // On receiving of a process' result, do all the integration
    case Result(chgs, chgT, chgDt) => {
      try {
      // Calculate this process' id
      val i = chgs.head.origin
      //println
      //println(s"$i, $chgT, $chgDt")
      // If this result is expected, process it (it can happen that a process got
      // deleted from the list of running processes, which means that its result
      // can safely be discarded)
      if (running.contains(Tuple3(i, chgT, chgDt))) {
        // Remove the current result from the list of running processes
        running -= Tuple3(i, chgT, chgDt)

        // Zip all the results and check for violations
        changes = chgs ::: changes

        // Some debug message that prints the state variables
        if (debug) println(s"Process $i: " + printStates(t, m(tCurr(i)), 6))
        model.calcDependencies(m(tCurr(i)))

        val dupl = m(tCurr(i)).dupl
        val violators = intersect(dupl, dupl.fieldPtrs, changes, t, pDt(i))
        if (violators.isDefined) {
          // Adjust current process' time step (up to next violation point / 2.0)
          pDt(i) = (violators.get._1 + (violators.get._2 - violators.get._1) / 2.0 - tCurr(i)) // 2.0
          tNext(i) = tCurr(i) + pDt(i)
          // Adjust all other processes' time step. Get ids first
          val violProcIds = violators.get._3.map(_.origin).distinct.filter(_ != i)
          if (debug) println("Violating! Mainly " + violProcIds.mkString("(", ", ", ")") + " plus i=" + i)
          // And adjust all dt's, tCurr's, ...
          // First remove all currently running process instances (as the current one is 
          // more important - TODO: this might not hold in general!!)
          running = running.filter(r => !violProcIds.contains(r._1))
          violProcIds.foreach { pid => 
            pDt(pid) = tCurr(i) - tPrev(pid) + pDt(i)
            tCurr(pid) = tPrev(pid)
            tNext(pid) = tPrev(pid) + pDt(pid)

            // Restart processes with new values
            running += Tuple3(pid, tCurr(pid), pDt(pid))
            processes(pid) ! Evolve(m(tCurr(pid)).dupl, tCurr(pid), pDt(pid))
            //println(s"$pid calculated with ${tCurr(pid)} -> ${pDt(pid)}")
          }

          // Restart current process
          running += Tuple3(i, tCurr(i), pDt(i))
          processes(i) ! Evolve(m(tCurr(i)).dupl, tCurr(i), pDt(i))
          //println(s"$i calculated with ${tCurr(i)} -> ${pDt(i)}")
          // Remove all changes of violating processes
          changes = changes.filterNot(c => violProcIds.contains(c.origin) | c.origin == i)
        } else {
          // If there were no violations, integrate all newly found changes and overwrite state history
          //m.foreach(x => println(printStates(x._1, x._2, 6)))
          //println(s"Trying to merge: ${tCurr(i)} -> ${tNext(i) - tCurr(i)}")
          m = merge(m, changes, tCurr(i), tNext(i) - tCurr(i))

          // Adjust i's times and the global time
          // Try to put pDt(i) back to original dt
          //println(s"${pDt(i)}, ${tPrev(i)}, ${tCurr(i)}, ${tNext(i)}")
          pDt(i) = math.min(pDt(i) * 2.0, dt)
          tPrev(i) = tCurr(i)
          tCurr(i) = tNext(i)
          tNext(i) = tCurr(i) + pDt(i)
          //println(s"${pDt(i)}, ${tPrev(i)}, ${tCurr(i)}, ${tNext(i)}")
          // Add to result vector
          //m += (t -> model.stateVector.dupl)
          //if (debug) println(printStates(t, model.stateVector, 6))

          // Get ids of smallest processes and set them off if they haven't already started
          val minCurrT = tCurr.min
          val minCurrIdxs = tCurr.zipWithIndex.filter(_._1 == minCurrT).map(_._2)
          val nextCurrT = tCurr.filter(_ != minCurrT).reduceOption(_ min _)

          minCurrIdxs.map{ mci =>
            val catchUpDt = if (nextCurrT.isDefined) 
              math.min(pDt(mci), (nextCurrT.get - minCurrT))
            else 
              pDt(mci)

            if (!(running.contains(Tuple3(mci, tCurr(mci), catchUpDt)) ||
                running.contains(Tuple3(mci, tCurr(mci), pDt(mci))))) {
              //println(s"$mci calculated with ${tCurr(mci)} -> $catchUpDt")
              tNext(mci) = tCurr(mci) + catchUpDt
              running += Tuple3(mci, tCurr(mci), catchUpDt)
              processes(mci) ! Evolve(m(tCurr(mci)).dupl, tCurr(mci), catchUpDt)
            } else {
              //println(s"$mci not calculated")
            }
          }

          t = tCurr.min
          if (t > maxTime) {
            running = collection.mutable.Set.empty[Tuple3[Int, Double, Double]]
            orig.foreach(_ ! TreeMap(m.toMap.toArray:_*))
            context.stop(self)
          }
          changes = changes.filter(c => c.tEnd > t)
        }
      }
      } catch {
        case e: Exception => {
            println(e)
            context.stop(self)
            System.exit(0)
          }
      }

      def merge(m: collection.mutable.Map[Double, model.StateType], changes: List[StormChange], startTime: Double, dt: Double) = {
        val mm = m.filter{case (t, s) => t <= startTime}
        //println(TreeMap(mm.toMap.toArray:_*))
        val dupl = mm(startTime).dupl      
        //println(s"keys of mm before are: " + mm.keys.toList.sorted.mkString(", "))
        intersectAndStore(mm, dupl, dupl.fieldPtrs, changes, startTime, dt)
        //println(s"keys of mm after are: " + mm.keys.toList.sorted.mkString(", "))
        mm.filter{case (t, s) => t >= startTime}.foreach{case (t, s) => model.calcDependencies(s)}
        //println(TreeMap(mm.toMap.toArray:_*))
        //println
        mm
      }

      def isRunning(id: Int) = {
        running.filter(_._1 == id).size > 0
      }
    }

  }

  def simulate(mod: StormModel): TreeMap[Double, StormState[_]] = {
    processes.foreach{ p =>
      //println(s"${p._1} calculated with 0.0 -> ${pDt(p._1)}")
      running += (Tuple3(p._1, 0.0, pDt(p._1)))
      p._2 ! Evolve(model.stateVector.dupl, 0.0, pDt(p._1))
    }

    TreeMap.empty[Double, StormState[_]]
  }
}