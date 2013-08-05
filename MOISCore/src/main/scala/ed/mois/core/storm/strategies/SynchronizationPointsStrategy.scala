/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.strategies


import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.util.Random
import scala.collection.immutable.TreeMap
import scala.concurrent._
import scala.concurrent.duration._

import ed.mois.core.storm._
import ed.mois.core.util.Refl
import ed.mois.core.storm.comm._

/**
 * Strategy that uses synchronization points to keep processes synchronized.
 */
class SynchronizationPointsStrategy(maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper with ChangeHelper {
  // Dispatcher for dealing with futures
  import context.dispatcher
  // Implicit timeout for dealing with actors and waiting for their results
  implicit val timeout = Timeout(60 seconds)

  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());

  def simulate(model: StormModel): TreeMap[Double, StormState[_]] = {
    val processes = model.processes.map(p => context.actorOf(Props(() => new StormDistrProcess(p()))))
    val m = collection.mutable.Map.empty[Double, StormState[_]]
    if (debug) println(printStatesHeader(model.stateVector, 6))

    for (t <- 0 to (maxTime / dt).toInt) {
      val time = t.toDouble * dt
      model.calcDependencies(model.stateVector)
      m += (time -> model.stateVector.dupl)
      if (debug) println(printStates(t.toDouble * dt, model.stateVector, 6))

      simStep(processes, 0 until processes.length toList, List.empty[StormChange], model, time, dt)
    }
    
    TreeMap(m.toMap.toArray:_*)
  }

  def simStep(processes: Array[ActorRef], procIds: List[Int], chgs: List[StormChange], model: StormModel, stepTime: Double, stepDt: Double) {
    val changes = processes.zipWithIndex.filter(p => procIds.contains(p._2)).map {
      p => p._1 ? Evolve(model.stateVector.dupl, stepTime, stepDt)
    }.toList

    val interResults = Future.sequence(for {
      c <- changes
      } yield c.mapTo[Result[_ <: StormState[_]]])

    val results = Await.result(interResults, 60 seconds)

    results.foreach(r => r.chgs.foreach(c => println(s"P ${c.origin}: ${c.chg}")))
    val violators = intersect(model.stateVector, model.stateVector.fieldPtrs, results.flatMap(_.chgs) ::: chgs)
    if (violators.isDefined) {
      val violIds = violators.get._3.map(_.origin)
      println(s"Violators are: $violIds; Trying to reduce time step to: ${stepDt / 2.0}, at $stepTime")
      simStep(processes, violIds, results.flatMap(r => r.chgs.filter(c => !violIds.contains(c.origin))), model, stepTime, stepDt / 2.0)
      simStep(processes, violIds, results.flatMap(r => r.chgs.filter(c => !violIds.contains(c.origin))), model, stepTime + stepDt / 2.0, stepDt / 2.0)
    }
  }
}