/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.comm

import akka.actor.ActorRef
import ed.mois._
import ed.mois.core.sim.chg._
import ed.mois.core.util._
import ed.mois.core.sim.prop.PropertyId
import ed.mois.core.sim.prop.TrackedProperty
import ed.mois.core.sim.prop.Property

sealed trait Message

case class SimInfo(title: String, desc: String, params: Map[String, Any], graph: SimGraph)

/** Starts simulation. */
case class RunSimulation() extends Message
case class ResetSimulation() extends Message
case class SimulationStart() extends Message
case class SimulationDone() extends Message

/** Links different actors. */
case class LinkResourcePool(pool: ActorRef) extends Message
case class LinkQuasiModule(module: ActorRef) extends Message

/** Synchronization message. */
case class Sync() extends Message

/** Initializes actor. */
case class Init(simulator: ActorRef, states: Map[String, ActorRef], processes: Map[String, ActorRef]) extends Message

/** Sends all the properties of a state. */
case class StateProps(props: collection.mutable.Map[String, TrackedProperty[Any]]) extends Message

/** Sends the name of a process or state. */
case class Name() extends Message
case class AssignId(id: Int) extends Message

case class RegForData(ref: ActorRef, listen: Boolean) extends Message
case class RegForProperty(ref: ActorRef, stateName: String, propName: String, listen: Boolean) extends Message

/** Sends a single data point. */
case class DataPoint(state: String, prop: String, time: Double, value: Any) extends Message

/** Sends multiple data points. */
case class DataPoints(dataPoints: List[DataPoint]) extends Message

/** Sends a parameter value. */
case class Param(name: String, value: Any) extends Message

/** Passes a follower. */
case class Follower(follower: ActorRef, name: String) extends Message

/** Sends dependencies on other states / processes. */
case class Dependencies(states: List[StateEntry]) extends Message
case class StateDependencies(processName: String, dependencies: Set[PropertyId]) extends Message
case class PropertyDependencies(processName: String, writeProps: Set[PropertyId], readProps: Set[PropertyId]) extends Message
case class Evolve(T0: Double, dT: Double, props: List[Property[Any]]) extends Message
case class ChangeRequest(procName: String, changes: List[Change])