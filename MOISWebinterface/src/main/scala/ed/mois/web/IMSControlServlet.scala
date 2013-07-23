/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.web

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
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
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import ed.mois.core.comm._
import ed.mois.core.sim._

class IMSControlServlet extends IMSControlStack
  with ScalateSupport with JValueResult
  with JacksonJsonSupport with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats
  implicit val timeout = Timeout(60 seconds)

  val system = Simulator.system
  val simulators: List[SimulationDescriptor] = List()

  get("/") {
    contentType = "text/html"
    ssp("/index", "simulators" -> simulators)
  }

  atmosphere("/imscontrol") {
    new AtmosphereClient with UiListener {
      var simulator: ActorRef = _
      val uiUpdater = system.actorOf(Props(new UiUpdater(this)))

      def receive = {
        case Connected =>
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) =>
        case TextMessage(text) => text match {
          case s: String => simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), true)
        }
        case JsonMessage(json) => {
          val t = (json \ "type").extract[String]
          t match {
            case "RequestSimInfo" => {
              val name = (json \ "simName").extract[String]
              simulator = simulators.filter(_.title == name).map(_.instantiate).head
              val simGraphFuture = (simulator ? RegForData(uiUpdater, true)).mapTo[SimInfo]
              simGraphFuture onFailure {
                case e: Exception => println(e)
              }
              Await.ready(simGraphFuture, timeout.duration)
              val simInfo = simGraphFuture.value.get.get

              send(write(simInfo))
            }
            case "StartSimulation" => simulator ! RunSimulation()
            case "ResetSimulation" => simulator ! ResetSimulation()
            case "UpdateParam" => simulator ! Param((json \ "param").extract[String], (json \ "value").extract[String])
            case "RegisterDataListener" => {
              val s = (json \ "dataPoint").extract[String]
              simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), true)
            }
            case "UnRegisterDataListener" => {
              val s = (json \ "dataPoint").extract[String]
              simulator ! RegForProperty(uiUpdater, s.split('.')(0), s.split('.')(1), false)
            }
            case _ =>
          }
          //broadcast(json)
        }
      }

      def updateUi(json: String) = {
        send(json)
      }
      def simDone {
        send(write("Simulation Done"))
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

trait UiListener {
  def updateUi(json: String)
  def simDone()
}

class UiUpdater(val ui: UiListener) extends Actor with ActorLogging {
  implicit protected val jsonFormats: Formats = DefaultFormats

  def receive = {
    case dp @ DataPoint(state, name, time, value) => ui.updateUi(write(dp))
    case DataPoints(dps) => dps.foreach(dp => ui.updateUi(write(dp)))
    case SimulationDone() => ui.simDone
    case _ => log.warning("Received unknown message in UiUpdater")
  }
}