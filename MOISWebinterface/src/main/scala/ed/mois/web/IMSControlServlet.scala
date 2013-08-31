/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.web

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util._

import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.jackson.Serialization.write
import org.json4s.jvalue2extractable
import org.json4s.jvalue2monadic
import org.scalatra.SessionSupport
import org.scalatra.atmosphere.AtmosphereClient
import org.scalatra.atmosphere.AtmosphereSupport
import org.scalatra.atmosphere.Connected
import org.scalatra.atmosphere.Disconnected
import org.scalatra.atmosphere.Error
import org.scalatra.atmosphere.JsonMessage
import org.scalatra.atmosphere.TextMessage
import org.scalatra.json.JValueResult
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.scalate.ScalateSupport
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import ed.mois.models.storm._
import ed.mois.core.storm._
import ed.mois.core.storm.strategies._

class IMSControlServlet extends IMSControlStack
  with ScalateSupport with JValueResult
  with JacksonJsonSupport with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats
  implicit val timeout = Timeout(60 seconds)

  /**
   * The different simulators available for simulation.
   */
  val simulators: List[SimulationDescriptor] = List(
    SimulationDescriptor("Brine Tank System", "A classical example of an ODE system.", () => {
      new StormSim {
        //override val simulationStrategy = () => new SynchronizationPointsStrategy(50.0, 0.01) {override val debug = true}
        val model = new BrineTankCascadeModel
        override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 50.0, 0.1) 
          {override val debug = false}
        override val printGnu = false
      }
    }),
    SimulationDescriptor("Recycled Brine Tank Cascade System", "A classical example of an ODE system, extended with a cascade.", () => {
      new StormSim {
        //override val simulationStrategy = () => new SynchronizationPointsStrategy(50.0, 0.01) {override val debug = true}
        val model = new RecycledBrineTankCascadeModel
        override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 50.0, 0.1) 
          {override val debug = false}
        override val printGnu = false
      }
    }),
    SimulationDescriptor("Sample ODE System", "A sample ODE system.", () => {
      new StormSim {
        //override val simulationStrategy = () => new SynchronizationPointsStrategy(50.0, 0.01) {override val debug = true}
        val model = new SampleODEModel
        override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 50.0, 0.1) 
          {override val debug = false}
        override val printGnu = false
      }
    }),
    SimulationDescriptor("Bollenbach System", "The Bollenbach whole cell model.", () => {
      new StormSim {
        //override val simulationStrategy = () => new SynchronizationPointsStrategy(50.0, 0.01) {override val debug = true}
        val model = new bollenbach.BollenbachModel(9769.0)
        override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 50.0, 0.1) 
          {override val debug = false}
        override val printGnu = false
      }
    }),
    SimulationDescriptor("Resource Processing System", "A resource processing system based on the Bollenbach whole cell model.", () => {
      new StormSim {
        //override val simulationStrategy = () => new SynchronizationPointsStrategy(50.0, 0.01) {override val debug = true}
        val model = new resourceprocessing.ResourceProcessingModel(9769.0)
        override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 50.0, 0.1) 
          {override val debug = false}
        override val printGnu = false
      }
    }))

  get("/") {
    contentType = "text/html"
    ssp("/index", "simulators" -> simulators)
  }

  atmosphere("/imscontrol") {
    // Handles the communication via WebSockets
    new AtmosphereClient {
      var simulator: StormSim = _
      var simInstantiator: () => StormSim = _

      def receive = {
        case Connected => 
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) =>
        case TextMessage(text) => text match {
          case s: String => //simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), true)
        }
        case JsonMessage(json) => {
          val t = (json \ "type").extract[String]
          t match {
            case "RequestSimInfo" => {
              val name = (json \ "simName").extract[String]

              simInstantiator = simulators.filter(_.title == name).map(_.instantiate).head
              simulator = simInstantiator()
              val simInfo = SimInfo(
                name, 
                simulators.filter(_.title == name).head.desc, 
                simulator.model.stateVector.fieldNames.map(n => (n._2, None)).toMap, 
                SimGraph(
                  List(StateEntry("State Vector", simulator.model.stateVector.fieldNames.map(n => PropertyId("State Vector", n._2, n._1)).toSet)), 
                  simulator.model.processes.map(create => {
                    val p = create()
                    ProcessEntry(p.name, simulator.model.stateVector.fieldNames.map(n => PropertyId("State Vector", n._2, n._1)).toSet, 
                      simulator.model.stateVector.fieldNames.map(n => PropertyId("State Vector", n._2, n._1)).toSet)
                  }).toList
                )
              )

              send(write(simInfo))
            }
            case "StartSimulation" => {
              val resultFuture = simulator.runSim
              resultFuture onComplete {
                case Success(result) => {
                  val dps = for { r <- result
                                  v <- r._2.fields } yield DataPoint("State Vector", r._2.fieldNames(v._1), r._1, v._2)
                  send(write(DataPoints(dps.toList)))
                  send(write("Simulation Done"))
                }
                case Failure(t) => 
              }

            }
            case "ResetSimulation" => simulator = simInstantiator()
            case "UpdateParam" => //simulator ! Param((json \ "param").extract[String], (json \ "value").extract[String])
            case "RegisterDataListener" => /*{
              val s = (json \ "dataPoint").extract[String]
              simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), true)
            }*/
            case "UnRegisterDataListener" => /*{
              val s = (json \ "dataPoint").extract[String]
              simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), false)
            }*/
            case _ =>
          }
          //broadcast(json)
        }
      }
    }
  }

  error {
    case t: Throwable => t.printStackTrace()
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}

/**
 * Different classes for state graphs and information passing.
 */
case class SimulationDescriptor(val title: String, val desc: String, val instantiate: () => StormSim)
case class SimInfo(title: String, desc: String, params: Map[String, Any], graph: SimGraph)
case class SimGraph(states: List[StateEntry], processes: List[ProcessEntry]) {
  override def toString = {
    "Simulation Graph: \n  States:\n    " + states.mkString("\n    ") + "\n  Processes:\n    " + processes.mkString("\n    ")
  }
}
case class StateEntry(name: String, props: Set[PropertyId])
case class ProcessEntry(name: String, writeProps: Set[PropertyId],
  readProps: Set[PropertyId])
case class PropertyId(state: String, name: String, var id: Int)

/** Sends a single data point. */
case class DataPoint(state: String, prop: String, time: Double, value: Any)

/** Sends multiple data points. */
case class DataPoints(dataPoints: List[DataPoint])