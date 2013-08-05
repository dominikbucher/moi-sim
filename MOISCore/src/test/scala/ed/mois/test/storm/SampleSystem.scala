/*
 * Contains a sample system to show some storm simulator aspects.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.storm

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._

import ed.mois.core.storm.strategies._
import ed.mois.core.storm._

object SampleSimRunner extends App {
  val sim = new StormSim {
    override val simulationStrategy = () => new IndepTimeScaleStrategy(50.0, 1.0) {override val debug = true}
    val model = new SampleModel
  }

  val results = sim.runSim
  Await.result(results, 60 seconds)
}

case class SampleState extends StormState[SampleState] {
  var r0 = field(500.0) >= 0.0
  var r1 = field(250.0) >= 0.0
  var r2 = field(250.0) >= 0.0
  var r3 = field("i")
  var r4 = field(true)
  var r5 = field(new Object)

  override def print = s"r0: $r0, r1: $r1, r2: $r2, r3: $r3, r4: $r4, r5: $r5"
}

class SampleModel extends StormModel {
  type StateType = SampleState

  def REACT_CONST = 0.00003
  def PROB_CONST = 0.3

  val title = "A Sample System Modifying Several Things"
  val desc = "A Sample System to show some nice properties of the simulator."
  val authors = "Dominik Bucher"
  val contributors = "Dominik Bucher"

  lazy val stateVector = SampleState()
  ++(() => new P1)
  ++(() => new P2)
  ++(() => new P3)
  ++(() => new P4)
  ++(() => new P5)
  ++(() => new P6)

  /*lazy val processes = Array(
    () => new P1, 
    () => new P2, 
    () => new P3, 
    () => new P4, 
    () => new P5, 
    () => new P6)*/

  import stateVector._
  override val observables = List(r0, r1, r2)
  def calcDependencies(st: SampleState) = {}

  class P1 extends StormProcess[SampleState] {
    def name = "P1"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val need = REACT_CONST * r0() * r1() * dt
      r0() -= need
      r1() -= need
      //println("S1: " + state.print)
    }
  }

  class P2 extends StormProcess[SampleState] {
    def name = "P2"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val need = REACT_CONST * r0() * r2() * dt
      r0() -= need
      r2() -= need
    }
  }

  class P3 extends StormProcess[SampleState] {
    def name = "P3"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r3() = "3"
      if (doIt) r4() = !r4()
    }
  }

  class P4 extends StormProcess[SampleState] {
    def name = "P4"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r3() = "4"
      if (doIt) r4() = !r4()
    }
  }

  class P5 extends StormProcess[SampleState] {
    def name = "P5"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r5() = new Object
    }
  }

  class P6 extends StormProcess[SampleState] {
    def name = "P6"
    def evolve(state: SampleState, t: Double, dt: Double) {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r5() = "Hi:)"
    }
  }
}