/*
 * Contains a general two resource consumer for testing.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.t1

import scala.collection.immutable.List
import ed.mois.macr.ChangeModelMacros
import ed.mois.core.sim.chg.Change
import ed.mois.macr.PropRefs
import ed.mois.core.sim.process._

class GeneralTwoResConsumer(name: String, res1: String, res2: String) extends Process(name) {
  //override def propRefs = ChangeModelMacros.setUpMessages
  val reactConst: Double = 0.0003

  val res1Pool = state[GeneralResPoolSkeleton](res1)
  val res2Pool = state[GeneralResPoolSkeleton](res2)

  def readProps = List(res1Pool.Res.id, res2Pool.Res.id)
  def writeProps = List(res1Pool.Res.id, res2Pool.Res.id)
  
  override def evolve(t0: Double, dt: Double): List[Change] = {
    val need = (reactConst * res1Pool.Res() * res2Pool.Res())

    res1Pool.Res() = res1Pool.Res() - need
    res2Pool.Res() = res2Pool.Res() - need

    AutoCollect
  }
}