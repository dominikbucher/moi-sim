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

object RecycledBrineTankCascadeSimRunner extends App {
  val sim = new StormSim {
    override val simulationStrategy = () => new SynchronizationPointsStrategy(40.0, 0.01) {override val debug = true}
    val model = new RecycledBrineTankCascadeModel
  }

  val results = sim.runSim
  Await.result(results, 60 seconds)
}

case class RecycledBrineTankCascadeState extends StormState[RecycledBrineTankCascadeState] {
  var x1 = field(5.0)
  var x2 = field(0.0)
  var x3 = field(0.0)

  override def print = s"x1: $x1, x2: $x2, x3: $x3"
}

class RecycledBrineTankCascadeModel extends StormModel {
  type StateType = RecycledBrineTankCascadeState

  val title = "A Brine Tank Cascade ODE System"
  val desc = "Modeled after the example in the book by Grant B. Gustafson (math.utah.edu)."
  val authors = "Grant B. Gustafson"
  val contributors = "Dominik Bucher"

  lazy val stateVector = RecycledBrineTankCascadeState()
  ++(() => new P1)
  ++(() => new P2)
  ++(() => new P3)
  ++(() => new Recycler)

  import stateVector._
  override val observables = List(x1, x2, x3)
  def calcDependencies(st: RecycledBrineTankCascadeState) = {}

  class P1 extends StormProcess[RecycledBrineTankCascadeState] {
    def name = "P1"
    def evolve(state: RecycledBrineTankCascadeState, t: Double, dt: Double) {
      import state._

      x1() += (-1.0 / 6.0 * x1()) * dt
    }
  }

  class P2 extends StormProcess[RecycledBrineTankCascadeState] {
    def name = "P2"
    def evolve(state: RecycledBrineTankCascadeState, t: Double, dt: Double) {
      import state._

      x2() += (1.0 / 6.0 * x1() - 1.0 / 3.0 * x2()) * dt
    }
  }

  class P3 extends StormProcess[RecycledBrineTankCascadeState] {
    def name = "P3"
    def evolve(state: RecycledBrineTankCascadeState, t: Double, dt: Double) {
      import state._

      x3() += (1.0 / 3.0 * x2() - 1.0 / 6.0 * x3()) * dt
    }
  }

  class Recycler extends StormProcess[RecycledBrineTankCascadeState] {
    def name = "Recycler"
    def evolve(state: RecycledBrineTankCascadeState, t: Double, dt: Double) {
      import state._

      x1() += (1.0 / 6.0 * x3()) * dt
    }
  }
}