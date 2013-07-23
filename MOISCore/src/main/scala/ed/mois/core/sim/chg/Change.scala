/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.chg

import scala.collection.mutable._
import scala.util.Random
import ed.mois.core.math.Mat

/**
 * A Change is an atomic operation on the state. It will be
 * performed if and only if all [[State]]s grant its execution.
 *
 * Execution can be denied if more than one [[Process]] try to
 * change a state property, or if not enough resources are available.
 *
 * @param originName The process that wants to induce the change on the state.
 * @param nTimes The number of times this change is induced on the state. This is within the duration,
 * i.e. if the duration is 3 seconds and nTimes is 3, every second one change takes place.
 * @param startTime The time when this change starts to take place.
 * @param duration How long the total change takes.
 * @param initChanges PropertyChanges that this change includes, sorted by state name.
 */
abstract class Change(val originName: String, val time: Double,
  val propertyChanges: List[PropertyChange]) {

  /**
   * Priority of this change. Not used atm, but could be used to give precedence to a set of changes.
   */
  var priority = 0

  /**
   * Identification number that uniquely identifies this change.
   */
  val id = Random.nextInt

  /**
   * Random number that is used to choose changes when conflicts occur.
   * Alternatively, the id could be used.
   */
  val randNr = Random.nextDouble

  /**
   * Merges two [[Change]]s together.
   * @param other The other [[Change]] to merge into this one.
   */
  def mergeChanges(other: Change): Option[Change] = { Some(this) }
}

/**
 * A flux is a change that takes place over time, namely over deltaT.
 */
case class Flux(override val originName: String, override val time: Double, val deltaT: Double,
  override val propertyChanges: List[PropertyChange]) extends Change(originName, time, propertyChanges) {

  /**
   * @see uk.ed.inf.changemodel.simulator.change.Change#mergeChanges(uk.ed.inf.changemodel.simulator.change.Change)
   *
   * You can add two fluxes together, but not an atomic change into a flux. Times are interpolated.
   */
  override def mergeChanges(other: Change): Option[Change] = {
    other match {
      case o: Flux => Some(Flux(originName,
        (time + o.time) / 2,
        (deltaT + o.deltaT) / 2,
        propertyChanges ++ o.propertyChanges))
      case o: Atomic => None
      case o => None
    }
  }
}

/**
 * An atomic change is a change that takes place instantaneously, at time 'time'.
 */
case class Atomic(override val originName: String, override val time: Double,
  override val propertyChanges: List[PropertyChange]) extends Change(originName, time, propertyChanges) {

  /**
   * @see uk.ed.inf.changemodel.simulator.change.Change#mergeChanges(uk.ed.inf.changemodel.simulator.change.Change)
   *
   * You can add a flux to an atomic change, which will condense the changes over deltaT to an instantaneous change.
   */
  override def mergeChanges(other: Change): Option[Change] = {
    other match {
      case o: Flux => Some(Atomic(originName,
        (time + (o.time + o.deltaT) / 2) / 2,
        propertyChanges ++ o.propertyChanges))
      case o: Atomic => Some(Atomic(originName,
        (time + o.time) / 2,
        propertyChanges ++ o.propertyChanges))
      case o => None
    }
  }
}

case class AutoCollectAndAtomizeChg extends Change("", 0.0, Nil)

case class AutoCollectChg extends Change("", 0.0, Nil)
