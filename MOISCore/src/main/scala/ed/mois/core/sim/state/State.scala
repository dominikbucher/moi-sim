/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.state

import ed.mois.macr.ChangeModelMacros._
import akka.actor._
import ed.mois.core.comm._
import ed.mois.core.sim.Simulator
import ed.mois.core.util.Verbosity
import ed.mois.core.sim.chg._
import ed.mois.core.sim._

/**
 * Abstract class state that defines general state structure.
 * Extends a [[StateSkeleton]] which defines properties of the state.
 * The state subclass is allowed to reference other states and the simulator object
 * as they reside within the same actor (i.e. on the same computer).
 *
 * @param name The name of this state.
 * @param simulator Reference to the enclosing simulator.
 */
abstract class State(val name: String, val simulator: Simulator) extends StateSkeleton {
  /**
   * The seed used in random operations.
   */
  val seed: Int

  /**
   * Creates a reference to another state within the simulator. As this other state could still be
   * non-existent at the time of creation of this state, the reference can also be null. This is
   * probably a bit dangerous as it means absolutely no operations on other states may be done
   * during state initialization.
   *
   * TODO Think of something better that resolves those dependencies in a simple way.
   *
   * @param name The name of the other state.
   * @param T The type of the other state.
   * @return A reference to the other state.
   */
  def state[T <: State: ClassManifest](name: String): T = {
    if (simulator != null && simulator.states.contains(name)) {
      simulator.states(name).asInstanceOf[T]
    } else {
      null.asInstanceOf[T]
    }
  }

  /**
   * Apply a list of changes to this state. After applying the tracking mechanism is reset.
   *
   * @param changes The list of changes to apply.
   */
  def applyChanges(changes: List[Change]) = {
    // Apply all changes
    changes.foreach(c => {
      applyChange(c)
    })
    // Reset change tracker
    resetChanges
  }

  /**
   * Applies a single change.
   *
   * @param change The change to apply.
   */
  def applyChange(change: Change) = {
    change.propertyChanges.filter(_.id.state == name).foreach(pc => props(pc.id.name).applyChange(pc))
  }

  def _init = {
    init
    update
  }
  
  /**
   * Initializes the state. Should be overwritten by subclasses to implement their
   * own initialization logic.
   */
  def init = {}

  /**
   * Updates the state, is called whenever state variables have changed.
   */
  def update = {}

  /**
   * Validate changes applied to state and checks for violations.
   * This method can return a subset of the changes if violations occurred.
   *
   * @param changes The changes to check for violations.
   * @return The complete set or a subset if violations occurred.
   */
  def validateChanges(changes: List[Change]): Map[Int, Change] = {
    var finalChanges = changes.map(c => c.id -> c).toMap
    // Property changes, sorted by property name, tuple includes (origin process, property change itself)
//    var propertyChanges = collection.mutable.Map.empty[String, List[Tuple2[Change, PropertyChange]]]
//    changes.foreach(c => c.basicChanges.filter(_._1 == name).foreach(bc => {
//      bc._2.foreach(pc => {
//        if (propertyChanges.contains(pc.prop)) {
//          propertyChanges(pc.prop) = (c, pc) :: propertyChanges(pc.prop)
//        } else {
//          propertyChanges(pc.prop) = List((c, pc))
//        }
//      })
//      (c.originName, bc._2)
//    }))
//
//    propertyChanges.map(pc => {
//      //println(s"property: $pc: ${pc._2}");
//      val violates = props(pc._1).violate_?(pc._2)
//      if (violates._1) {
//        // Now redistribute
//        println("violation at property: " + props(pc._1));
//        pc._2.foreach(c => c._1.nTimes = math.floor(c._1.nTimes / violates._2).toInt)
//      }
//    })

    finalChanges
  }
}