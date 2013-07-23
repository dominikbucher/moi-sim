/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim

import java.util.concurrent.TimeUnit
import scala.Array.canBuildFrom
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import ed.mois.core.comm._
import ed.mois.core.sim.chg._
import ed.mois.core.sim.process.Process
import ed.mois.core.sim.prop._
import ed.mois.core.sim.state.State
import ed.mois.core.util._
import ed.mois.core.sim.integr._

trait SimulationDescriptor {
  def title: String
  def desc: String
  def instantiate: ActorRef
}

/**
 * Singleton that gives access to the main actor system (used by user interfaces).
 *
 * TODO This is not quite so good for really distributed systems...
 */
object Simulator {
  val system = ActorSystem("IMSSystem")

  private var uniqueId = 0
  def getUniqueId = {
    val id = uniqueId
    uniqueId += 1
    id
  }
}

/**
 * Simulator class. Handles all the simulation in the background. Manages states,
 * processes, evolving of simulation, logging of data, ...
 *
 * @param maxTime Maximal time this simulator will run.
 */
abstract class Simulator(val title: String, val desc: String,
  initParams: Map[String, Any]) extends Actor with ActorLogging {

  type Params = collection.mutable.Map[String, Any]

  /**
   * Alternative constructor that defaults the step size to 1.0 seconds.
   *
   * @param maxTime The maximal time the simulator runs.
   */
  def this(title: String, desc: String, maxTime: Double, stepSize: Double) {
    this(title, desc, Map("maxTime" -> maxTime, "stepSize" -> stepSize))
  }

  def this(title: String, desc: String, maxTime: Double) {
    this(title, desc, Map("maxTime" -> maxTime, "stepSize" -> 1.0))
  }

  val EPSILON_TIME = 1e-6
  val EPSILON_PROP = 1e-6
  val EPSILON_OPT_TOLERANCE_FEAS = 1e-9
  val EPSILON_OPT_TOLERANCE = 1e-9

  // Dispatcher for dealing with futures
  import context.dispatcher
  // Implicit timeout for dealing with actors and waiting for their results
  val setupTimeout = Timeout(60 seconds)
  implicit val timeout = Timeout(10 seconds)

  val integr: Integrator = new StepbackIntegrator(this)

  /**
   * Map of processes involved in simulation.
   */
  case class ProcessRef(id: Int, actor: ActorRef, delta: Double, t: Double)
  val processes = collection.mutable.Map[String, ProcessRef]()

  /**
   * Map of states involved in simulation.
   */
  val states = collection.mutable.Map[String, State]()

  /**
   * List of properties involved in simulation.
   */
  var properties = collection.mutable.Map.empty[Int, Property[Any]]

  /**
   * Graph how processes depend on states and properties.
   */
  var simGraph: SimGraph = _

  val params: Params = collection.mutable.Map("maxTime" -> 25.0, "stepSize" -> 1.0)
  initParams.foreach(ip => params(ip._1) = ip._2)

  /**
   * Who initially started the simulator.
   */
  var origin: ActorRef = _

  /**
   * Collection of data listeners (e.g. user interfaces).
   */
  var dataListeners = List[DataListener]()

  /**
   * Adds a process to the simulation. The process is invoked as an actor.
   *
   * @param creator The function that creates the process.
   */
  private def addProcess(creator: => Process) = {
    val tmpProc = Simulator.system.actorOf(Props(creator))
    tmpProc ! AssignId(processes.size)
    val procNameFut = tmpProc ? Name()
    val procName = Await.result(procNameFut.mapTo[String], timeout.duration)

    processes += (procName -> ProcessRef(processes.size, tmpProc,
      getParam("stepSize").getOrElse("1.0").toDouble, 0.0))
  }

  /**
   * Adds a state to the simulation.
   *
   * @param ss The state to add.
   */
  private def addState(ss: State) = {
    states += (ss.name -> ss)
    // Update property id's
    ss.props.foreach(p => {
      p._2.id.id = p._2.id.id + properties.size
      properties.put(p._2.id.id, p._2)
    })
  }

  /**
   * Short way to add states. Allows method chaining.
   *
   * @param ss The state to add.
   * @return The simulation object.
   */
  def ++(ss: State) = {
    addState(ss)
    this
  }

  /**
   * Short way to add processes. Allows method chaining.
   *
   * @param creator The function that creates the process to add.
   * @return The simulation object.
   */
  def ++(creator: => Process) = {
    addProcess(creator)
    this
  }

  def property(fullName: String) = {
    states(PropertyId.decompose(fullName)._1).props(PropertyId.decompose(fullName)._2)
  }

  def property(state: String, name: String) = {
    states(state).props(name)
  }

  def property(id: Int) = {
    properties(id)
    //properties.filter(_.id == id).map(p => states(p.state).props(p.name)).head
  }

  def propertyVector() = {
    properties.map(p => p._2).toArray
  }

  def propertyValueVector() = {
    properties.map(p => p._2()).toArray
  }

  def propertyVectorCopy(): Array[Property[Any]] = {
    val r = Array.ofDim[Property[Any]](properties.size)
    properties.foreach(p => r(p._1) = p._2.copy)
    r
  }

  def getParam(name: String): Option[String] = {
    if (params.contains(name)) Some(params(name).toString)
    else None
  }

  /**
   * Updates all data listeners by sending current data points to them.
   *
   * @param simTime The current simulation time.
   */
  private def _updateDataListeners(simTime: Double) {
    for {
      dl <- dataListeners
    } {
      dl.ref ! DataPoints({
        for (p <- dl.props)
          yield DataPoint(p._1, p._2, simTime, property(p._1, p._2)())
      }.toList)
    }
  }

  /**
   * Initializes simulation, sets up dependencies between processes and states.
   */
  private def _initSim {
    // Build simulation graph
    // Assign unique identifiers to all properties
    if (simGraph == null) createSimGraph()

    // Initialize states
    states.foreach(_._2._init)
    log.info(s"States [${states.map(_._1).mkString(", ")}] initialized.")

    // Initialize processes

    log.info(s"Processes [${processes.map(_._1).mkString(", ")}] registered.")

    // Hook for user to specify additional initialization steps
    initSim
    log.info(s"Simulator initialized.")

    // Doing rest and proceeding to simulation
    log.info("Proceeding to simulation.")
  }

  /**
   * Creates a simulation graph depicting variable flows.
   */
  def createSimGraph() {
    // Build state graph
    val stateGraph = states.map { case (name, state) => StateEntry(name, state.props.map(_._2.id).toSet) }.toList

    val procDeps = processes.map(p => p._2.actor ? Dependencies(stateGraph)).toList
    val procDepsDecoded = Future.sequence(for {
      pd <- procDeps
    } yield pd.mapTo[PropertyDependencies])
    val result = Await.result(procDepsDecoded, setupTimeout.duration)
    // Build process and state graphs
    val procGraph = result.map(p => ProcessEntry(p.processName, p.writeProps, p.readProps))
    // Store in simulation graph object
    simGraph = SimGraph(stateGraph, procGraph)
    println(simGraph)
  }

  def resetSim = {
    properties.foreach(p => p._2.reset)
    //states.foreach(s => s._2.props.foreach(p => p._2.reset))
  }

  /**
   * Initializes the simulation. Should be overwritten by user of simulation engine.
   */
  def initSim = {}

  /**
   * Hook to interact with simulator before evolve methods are called.
   */
  def interceptBefore(t0: Double, stepSize: Double) = {}

  /**
   * Hook to interact with simulator after evolve methods are called.
   */
  def interceptAfter(t0: Double, stepSize: Double) = {}

  /**
   * Registers an actor for data updates. Which properties it listens to has
   * to be further specified.
   *
   * @param a The actor that listens for the updates.
   */
  def registerForData(a: ActorRef, listen: Boolean) {
    if (listen) dataListeners = DataListener(a.path.name, a, List[(String, String)]()) :: dataListeners
    else dataListeners = dataListeners.filter(_.ref != a)
  }

  /* (non-Javadoc)
 * @see akka.actor.Actor#preStart()
 */
  override def preStart {
    log.debug("Starting Simulator")
  }

  /* (non-Javadoc)
 * @see akka.actor.Actor#preRestart(java.lang.Throwable, scala.Option)
 */
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse("No specific message given."))
  }

  /**
   * Runs the simulation.
   */
  def runSim() = {
    val startTime = System.currentTimeMillis()
    var simTime = 0.0
    val maxTime = getParam("maxTime").getOrElse("15.0").toDouble
    val stepSize = getParam("stepSize").getOrElse("1.0").toDouble

    // Farm out
    while (simTime < maxTime) {
      log.info("Starting new simulation round: " + simTime)
      states.foreach(_._2.update)
      // Handle all the data listeners
      _updateDataListeners(simTime)

      // Hook to intercept before evolve starts
      interceptBefore(simTime, stepSize)

      // Call evolve methods
      val procChanges = simGraph.processes.map { p =>
        val proc = processes(p.name)
        val propList = p.readProps.map(p => properties(p.id)).toList
        proc.actor ? Evolve(proc.t, proc.delta, propList)
      }.toList

      // Request results
      val changeRequests = Future.sequence(for {
        pc <- procChanges
      } yield pc.mapTo[ChangeRequest])
      // And wait for them
      val chgReqs = Await.result(changeRequests, timeout.duration)
      println(s"Gotten following reqs: $chgReqs")
      simTime += integr.integrate(chgReqs)

      // Hook to intercept after evolve
      interceptAfter(simTime, stepSize)
    }

    // Shutting down system
    val executionTime = System.currentTimeMillis() - startTime
    log.info("Simulation done (T = " +
      TimeUnit.MILLISECONDS.toMinutes(executionTime) + " min, " +
      (TimeUnit.MILLISECONDS.toSeconds(executionTime) -
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))) +
        " sec), shutting down.")
    for {
      dl <- dataListeners
    } {
      dl.ref ! SimulationDone()
    }
    Simulator.system.shutdown
    context.system.shutdown
  }

  /**
   * @see akka.actor.Actor#receive()
   *
   * Receives messages that control the simulator, namely:
   * - RunSimulation(): Starts the simulation.
   * - RegisterForData(ref): Registers listener ref for updates on data. This message
   *   will be answered with a simulation graph that describes all states and their
   *   properties, and which processes access which properties.
   * - RegisterForProperty(): Registers a listener for a certain property.
   */
  def receive = {
    case RunSimulation() => {
      log.info("Received RunSimulation() command, starting simulation.");
      origin = sender
      _initSim
      runSim
    }
    case ResetSimulation() => resetSim
    case RegForData(ref, listen) => {
      registerForData(ref, listen)
      if (simGraph == null) createSimGraph()
      if (listen) sender ! SimInfo(title, desc, params.toMap, simGraph)
    }
    case RegForProperty(actor, stateName, propName, listen) => {
      if (listen) {
        dataListeners.filter(_.ref == actor).foreach(dl => dl.props = (stateName, propName) :: dl.props)
        log.info("{} registered for {}.{}", actor.path, stateName, propName)
      } else {
        dataListeners.filter(_.ref == actor).foreach(dl => dl.props = dl.props.diff(List((stateName, propName))))
        log.info("{} unregistered from {}.{}", actor.path, stateName, propName)
      }
    }
    case Param(name, value) => {
      params(name) = value
      println(s"updating $name with $value")
    }
    case x => {
      log.warning("Received unknown message: {}", x)
    }
  }
}