/*
 * Contains a sample system to show some storm simulator aspects.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.models.storm

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._

import ed.mois.core.storm.strategies._
import ed.mois.core.storm._

object SampleODESimRunner extends App {
  val sim = new StormSim {
    override val simulationStrategy = () => new IndepTimeScaleStrategy(50.0, 0.05) {override val debug = false}
    val model = new SampleODEModel
  }

  val results = sim.runSim
  Await.result(results, 60 seconds)
}

case class SampleODEState extends StormState[SampleODEState] {
  var x1 = field(25.0)
  var x2 = field(50.0)

  override def print = s"x1: $x1, x2: $x2"
}

/**
 * An example system that does random and differential stuff on the state. 
 * Used primarily for debugging and showing how strategies work.
 */
class SampleODEModel extends StormModel {
  type StateType = SampleODEState

  val title = "A Sample ODE System"
  val desc = "A Sample System to show some nice properties of the simulator."
  val authors = "Dominik Bucher"
  val contributors = "Dominik Bucher"

  lazy val stateVector = SampleODEState()
  ++(() => new P1)
  ++(() => new P2)

  import stateVector._
  override val observables = List(x1, x2)
  def calcDependencies(st: SampleODEState) = {}

  class P1 extends StormProcess[SampleODEState] {
    def name = "P1"
    def evolve(state: SampleODEState, t: Double, dt: Double) {
      import state._

      x1() += (-0.3 * x1() - 0.4 * x2()) * dt
    }
  }

  class P2 extends StormProcess[SampleODEState] {
    def name = "P2"
    def evolve(state: SampleODEState, t: Double, dt: Double) {
      import state._

      x2() += (-0.5 * x1() - 0.8 * x2()) * dt
    }
  }
}