/*
 * Contains a general resource consumer for testing.
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

class GeneralResConsumer(name: String, res: String) extends Process(name) {
  //override def propRefs = ChangeModelMacros.setUpMessages
  val reactConst: Double = 0.0003

  val resPool = state[GeneralResPoolSkeleton](res)
  
  def readProps = List(resPool.Res.id)
  def writeProps = List(resPool.Res.id)

  override def evolve(t0: Double, dt: Double): List[Change] = {
    val need = (reactConst * resPool.Res()).toInt

    resPool.Res() = resPool.Res() - need

    AutoCollect
  }
}