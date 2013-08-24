package ed.mois.test.storm.karrlike

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._

import ed.mois.core.storm.strategies._
import ed.mois.core.storm._

object KarrlikeSimRunner extends App {
  val sim = new StormSim {
    val model = new KarrlikeModel
    // Override the default simulation strategy to use a smash strategy with debug output
    //override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 30.0, 1.0) {
    //override val simulationStrategy = () => new SynchronizationPointsStrategy(30.0, 1.0) {
    //override val simulationStrategy = () => new IndepTimeScaleStrategy(30.0, 1.0) {
    //override val simulationStrategy = () => new SmashStrategy(30.0, 1.0) {
    override val simulationStrategy = () => new KarrStrategy(30.0, 1.0) {
      override val debug = false
    }
  }

  val results = sim.runSim
  Await.result(results, 360 seconds)
}