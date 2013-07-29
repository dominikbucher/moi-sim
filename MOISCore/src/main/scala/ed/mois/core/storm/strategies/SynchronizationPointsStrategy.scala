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
class SynchronizationPointsStrategy(maxTime: Double, dt: Double) extends SimulationStrategy with StrategyHelper {
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

      val changes = processes.map {
        p => p ? Evolve(model.stateVector.dupl, time, dt)
      }.toList

      val interResults = Future.sequence(for {
        c <- changes
        } yield c.mapTo[Result[_ <: StormState[_]]])

      val results = Await.result(interResults, 60 seconds)

      //results.foreach(r => println(r.fields))
      for (f <- model.stateVector.fields) {
        // model.stateVector.fields(f._1) = 
        //   mash(f._2, results.map(_.state.fields(f._1)).toArray)
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