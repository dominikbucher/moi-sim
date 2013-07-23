/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

import akka.actor._

import ed.mois.core.storm._
import ed.mois.core.storm.comm._

/**
 * A light wrapper around processes to make them distributed. 
 */ 
class StormDistrProcess[T <: StormState[_]](val proc: StormProcess[T]) 
  extends Actor with ActorLogging {

  def receive = {
  	// A little hacky typecast here - investigate! (Problem is that 
  	// type is lost over message)
  	case Evolve(s, t, dt) => sender ! Result[T](proc._evolve(s.asInstanceOf[T], t, dt), t, dt)
  }
}
