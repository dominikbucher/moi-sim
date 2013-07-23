/*
 * Contains a sample system to show the tiny simulator.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.tiny.t1

import ed.mois.core.tiny.strategies._
import ed.mois.core.tiny._

object SampleSystem extends App {
  (new SampleSystem).runSim
}

case class SampleState extends StateVector[SampleState] {
  var r0 = field(500.0) >= 0.0
  var r1 = field(250.0) >= 0.0
  var r2 = field(250.0) >= 0.0
  var r3 = field("i")
  var r4 = field(true)
  var r5 = field(new Object)

  override def print = s"r0: $r0, r1: $r1, r2: $r2, r3: $r3, r4: $r4, r5: $r5"
}

class SampleSystem extends Tiny[SampleState] {
  def REACT_CONST = 0.003
  def PROB_CONST = 0.3

  lazy val stateVector = SampleState()
  lazy val processes = Array(new P1, new P2, new P3, new P4, new P5, new P6)

  lazy val simulationStrategy = new StepadjustStrategy[SampleState](25.0, 1.0, 0.001, 2.0)
  val title = "A Sample System Modifying Several Things"
  import stateVector._
  override val observables = List(r0, r1, r2)
  def calcDependencies(st: SampleState) = {}

  class P1 extends TinyProcess[SampleState] {
    def name = "P1"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val need = REACT_CONST * r0() * r1() * dt
      r0() = r0() - need
      r1() = r1() - need
      //println("S1: " + state.print)
      state
    }
  }

  class P2 extends TinyProcess[SampleState] {
    def name = "P2"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val need = REACT_CONST * r0() * r2() * dt
      r0() = r0() - need
      r2() = r2() - need
      state
    }
  }

  class P3 extends TinyProcess[SampleState] {
    def name = "P3"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r3() = "3"
      if (doIt) r4() = !r4()
      state
    }
  }

  class P4 extends TinyProcess[SampleState] {
    def name = "P4"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r3() = "4"
      if (doIt) r4() = !r4()
      state
    }
  }

  class P5 extends TinyProcess[SampleState] {
    def name = "P5"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r5() = new Object
      state
    }
  }

  class P6 extends TinyProcess[SampleState] {
    def name = "P6"
    def evolve(state: SampleState, t: Double, dt: Double): SampleState = {
      import state._

      val doIt = (PROB_CONST * dt >= math.random)
      if (doIt) r5() = "Hi :)"
      state
    }
  }
}