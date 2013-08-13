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
  val tCurr = Array.fill(processes.size)(0.0)
  val tPrev = Array.fill(processes.size)(0.0)
  val tNext = Array.fill(processes.size)(dt)
  val pDt = Array.fill(processes.size)(dt)

  var running = collection.mutable.Set.empty[Tuple3[Int, Double, Double]]
  // Initialize result map
  var m = collection.mutable.Map.empty[Double, model.StateType]
  m += (0.0 -> model.stateVector.dupl)
  if (debug) println(printStatesHeader(model.stateVector, 6))
  var t = 0.0

  var changes = List.empty[StormChange]

  var orig: Option[ActorRef] = None

  override def receive = {
    case RunSimulation(model) => {
      orig = Some(sender)
      simulate(model)
    }
    case Result(chgs, chgT, chgDt) => {
      val i = chgs.head.origin
      if (running.contains(Tuple3(i, chgT, chgDt))) {
        running -= Tuple3(i, chgT, chgDt)

        if (debug) println(s"Process $i: " + printStates(t, m(tCurr(i)), 6))
        model.calcDependencies(m(tCurr(i)))

        // Zip all the results and check for violations
        changes = chgs ::: changes

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
          running = running.filter(r => !violProcIds.contains(r._1))
          violProcIds.foreach { pid => 
            pDt(pid) = tCurr(i) - tPrev(pid) + pDt(i)
            tCurr(pid) = tPrev(pid)
            tNext(pid) = tPrev(pid) + pDt(pid)

            running += Tuple3(pid, tCurr(pid), pDt(pid))
            processes(pid) ! Evolve(m(tCurr(pid)).dupl, tCurr(pid), pDt(pid))
          }

          running += Tuple3(i, tCurr(i), pDt(i))
          processes(i) ! Evolve(m(tCurr(i)).dupl, tCurr(i), pDt(i))
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

          if (tCurr(i) < maxTime && !isRunning(i)) {
            running += Tuple3(i, tCurr(i), pDt(i))
            processes(i) ! Evolve(m(tCurr(i)).dupl, tCurr(i), pDt(i))
          } else if (running.size == 0) {
            orig.foreach(_ ! TreeMap(m.toMap.toArray:_*))
            context.stop(self)
          }

          t = tCurr.min
          changes = changes.filter(c => c.tEnd > t)
        }
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

      def isRunning(id: Int) = {
        running.filter(_._1 == id).size > 0
      }
    }

  }

  def simulate(mod: StormModel): TreeMap[Double, StormState[_]] = {
    processes.foreach{ p =>
      running += (Tuple3(p._1, t, dt))
      p._2 ! Evolve(model.stateVector.dupl, t, dt)
    }

    TreeMap.empty[Double, StormState[_]]
  }
}