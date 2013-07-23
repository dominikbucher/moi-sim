/*
 * Contains test implementation for the core system.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.t1

import akka.actor._
import scala.concurrent._
import ed.mois.core.sim.Simulator
import ed.mois.core.sim.SimulationDescriptor
import ed.mois.kb.Molecular
import ed.mois.core.comm.RunSimulation

object RunTest1 {
  val simSteps = 25

  def main(args: Array[String]) {
    val sim = TestImpl1.instantiate
    sim ! RunSimulation()
  }
}

object TestImpl1 extends SimulationDescriptor {
  def title = "Simulator Reference Implementation 1"
  def desc =
    "This simulator implements a simple system consisting of 3 different resources, A, B and ATP. Two processes consume the resources, the first one A and ATP, the second one B and ATP."
  val initParams = Map("maxTime" -> 25.0, "stepSize" -> 1.0)
  def instantiate = Simulator.system.actorOf(Props(new SimulatorRefImpl1(title, desc, initParams)))
}

/**
 * Implements a simple biological system consisting of two processes:
 *
 * P1: ATP + M1 -> .
 * P2: ATP + M2 -> .
 *
 * The processes are implemented in the [[SimpleResConsumer]] class.
 */
class SimulatorRefImpl1(title: String, desc: String, params: Map[String, Any]) extends Simulator(title, desc, params) {
  ++(new GeneralResPool("SimpleAPool", this))
  ++(new GeneralResPool("SimpleBPool", this))
  ++(new GeneralResPool("SimpleATPPool", this))
  ++(new GeneralTwoResConsumer("AConsumer", "SimpleAPool", "SimpleATPPool"))
  ++(new GeneralTwoResConsumer("BConsumer", "SimpleBPool", "SimpleATPPool"))

  Molecular.getWeightFromEmpiricalFormula("C2H3O2")
  lazy val simpleAPool = states("SimpleAPool").asInstanceOf[GeneralResPool]
  lazy val simpleBPool = states("SimpleBPool").asInstanceOf[GeneralResPool]
  lazy val simpleATPPool = states("SimpleATPPool").asInstanceOf[GeneralResPool]

  override def interceptBefore(t0: Double, stepSize: Double) = {
    log.info("A: {}", simpleAPool.Res())
    log.info("B: {}", simpleBPool.Res())
    log.info("ATP: {}", simpleATPPool.Res())
  }
}