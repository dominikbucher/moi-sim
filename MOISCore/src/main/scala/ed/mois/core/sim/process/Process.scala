/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.process

import akka.actor._
import ed.mois.macr._
import ed.mois.core.comm._
import ed.mois.core.kb.KBAccess
import ed.mois.core.math.CMMath
import ed.mois.core.sim.chg._
import ed.mois.core.sim.state.StateDependency
import ed.mois.core.sim.prop.Property
import ed.mois.core.sim.prop.TrackedProperty
import ed.mois.core.sim.prop.PropertyId

abstract class Process(val name: String) extends Actor
  with ActorLogging with KBAccess with StateDependency with CMMath {
  var pid = 0

  /**
   * Lists of referenced property names, separated by update properties and apply properties.
   * Update properties are properties that get written by this process, apply properties only get read.
   *
   * TODO Include not only property name, but also state it belongs to.
   *
   * @return The referenced properties by update or apply.
   */
  // TODO Make abstract, so subprocesses will have to call it (the only reason why that's not been done yet is because I didn't want to do that for every child process).
  //def propRefs: PropRefs

  /**
   * Init method that can do some initial stuff whenever the process is created.
   * This can be overwritten by the subclass, however keep in mind that there is
   * absolutely no state within the processes.
   */
  //def init = {}

  /**
   * Convenience object to inject at end of evolve that automatically collects all changes.
   */
  val AutoCollect = List(AutoCollectAndAtomizeChg())
  /**
   * Convenience object to inject at end of evolve that automatically collects all changes
   * and atomizes them (i.e. splits them in their smallest possible elementary changes, which
   * then can be independently accepted or rejected by the simulator).
   */
  val AutoCollectAndAtomize = List(AutoCollectChg())

  /**
   * Core method that produces a list of changes on the state.
   *
   * @return The list of changes this process would like to induce on the state.
   */
  def evolve(t0: Double, dt: Double): List[Change]
  
  def readProps: List[PropertyId]
  def writeProps: List[PropertyId]

  /**
   * Internal method that is called by the simulator. Splits evolve in pre- and after-evolve.
   *
   * @return The list of changes produced and processed by the different evolve methods.
   */
  private def _evolve(t0: Double, dt: Double): List[Change] = {
    _preEvolve
    _afterEvolve(t0, dt, evolve(t0, dt))
  }

  /**
   * Prior to the real evolve method, do some adjustments on the "state" of this process.
   * The state of this process in this case is only not completely reset for performance issues.
   */
  private def _preEvolve = {
    // Reset all collected changes of the internal states
    states.foreach(_._2.resetChanges)
  }

  /**
   * The internal after evolve method does some postprocessing on the list of changes.
   * Depending on what the programmer of the process wants this can be an atomizing of changes,
   * a collection of them, a splitting or distribution over time, ...
   *
   * @param changes The list of changes produced by the evolve method.
   * @return The processed list of changes.
   */
  private def _afterEvolve(t0: Double, dt: Double, changes: List[Change]): List[Change] = {
    changes match {
      // AutoCollectAndAtomize sums up all changes and takes their gcd, leading to a smallest
      // set of possible changes that can either be accepted or rejected.
      case AutoCollectAndAtomize => {
        // Collect all changes from the states
        val autoChanges = states.flatMap(_._2.collectChanges).toList
        List(Atomic(name, t0, autoChanges))
        //        // Map all of them to mathematical values, creating an array (for calculating gdc)
        //        val changeValues = autoChanges.flatMap(ac => {
        //          ac._2.flatMap(pc => {
        //            pc match {
        //              case DoubleChange(name, value) => Array(math.abs(pc.asInstanceOf[DoubleChange].value.toInt))
        //              case IntChange(name, value) => Array(math.abs(pc.asInstanceOf[IntChange].value))
        //              case BooleanChange(name, value) => Array(1)
        //              case MatChange(name, value) => value.abs.toIntArray
        //            }
        //          })
        //        }).toArray
        //
        //        // TODO use actors to calculate gcd
        //        // Calculate gcd of all array values
        //        val need = if (changeValues.length == 0) 1 else math.max(gcd(changeValues), 1)
        //        
        //        // Remap to new set of property changes, which are scaled
        //        val changes = autoChanges.map(ac => ac._1 -> ac._2.map(pc => {
        //          pc match {
        //            case DoubleChange(name, value) => DoubleChange(pc.prop, pc.asInstanceOf[DoubleChange].value / need).asInstanceOf[PropertyChange]
        //            case IntChange(name, value) => IntChange(pc.prop, pc.asInstanceOf[IntChange].value / need).asInstanceOf[PropertyChange]
        //            case BooleanChange(name, value) => BooleanChange(pc.prop, pc.asInstanceOf[BooleanChange].value).asInstanceOf[BooleanChange]
        //            case MatChange(name, value) => MatChange(pc.prop, pc.asInstanceOf[MatChange].value @/ need).asInstanceOf[PropertyChange]
        //          }
        //        }))
        //
        //        // Set the total times the complete change is applied to "need"
        //        List(Atomic(name, need, t0, dt, false, changes))
      }
      // AutoCollect sums up all changes and builds one big change request
      case AutoCollect => {
        val autoChanges = states.flatMap(_._2.collectChanges).toList
        List(Flux(name, t0, dt, autoChanges))
      }
      case _ => {
        changes
      }
    }

  }

  /**
   * @see akka.actor.Actor#receive()
   *
   * Receives different messages, namely:
   * - Dependencies(): Asks for state properties this process depends upon.
   *   They are either update or apply properties, which means they are written to or only read.
   * - Evolve(t0, dt, props): Performs this process' evolve method on the given properties.
   *   Starting point is t0, time step is dt.
   * - Name(): Asks for this process' name.
   */
  def receive = {
    case Dependencies(stateEntries) => {
      stateEntries.foreach(se => se.props.foreach(p => {
        if (states.contains(se.name))
          states(se.name).props(p.name).id.id = p.id
      }))

//      val writeProps = propRefs.writeProps.map { p =>
//        states.filter(_._2.props.contains(p)).head._2.props(p).id
//      }.toSet
//      val readProps = propRefs.readProps.map { p =>
//        states.filter(_._2.props.contains(p)).head._2.props(p).id
//      }.toSet
      //      val updateDeps = propRefs.updateProps.map(pr => {
      //        var state = states.filter(_._2.props.contains(pr)).head
      //        state._1 -> pr
      //      }).groupBy(_._1).mapValues(_.map(_._2))
      //      val applyDeps = propRefs.applyProps.map(pr => {
      //        var state = states.filter(_._2.props.contains(pr)).head
      //        state._1 -> pr
      //      }).groupBy(_._1).mapValues(_.map(_._2))
      sender ! PropertyDependencies(name, writeProps.toSet, readProps.toSet)
    }
    case Evolve(t0, dt, props) => {
      props.map(p => states(p.id.state).overrideProp(p))
      sender ! ChangeRequest(name, _evolve(t0, dt))
    }
    case Name() => {
      sender ! name
    }
    case AssignId(id) => {
      pid = id
    }
    case x => {
      log.warning("Received unknown message: {}", x)
    }
  }
}