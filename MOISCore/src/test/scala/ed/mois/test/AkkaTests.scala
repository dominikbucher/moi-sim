/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */
package ed.mois.test

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.immutable.TreeMap
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/**
 * Tests some aggregation stuff (communication speed, needed for some arguments in the thesis).
 */
object AkkaTests extends App {
  case class Msg {
  	var a = Array.ofDim[Any](2500)
  }

  class Measurer(responder: ActorRef) extends Actor {
  	val startTime = System.currentTimeMillis
  	var counter = 1
	  def receive = {
	  	case "start" => {
	  		(1 to 10000).foreach(i => responder ! Msg())
	  	}
	  	case a => {
	  		counter += 1
	  		if (counter == 10000) {
		  		println(System.currentTimeMillis - startTime)
		  		system.shutdown
	  		}
	  	}
	  }
	}

  class Responder extends Actor {
	  def receive = {
	  	case a => sender ! a
	  }
	}


  val system = ActorSystem("system")
  val responder = system.actorOf(Props(new Responder()))
  val measurer = system.actorOf(Props(new Measurer(responder)))

  measurer ! "start"
}