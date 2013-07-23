/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.comm

import akka.actor.ActorRef
import ed.mois.core.util.StringToSetMap

case class DataListener(name: String, ref: ActorRef,
  var props: List[(String, String)]) {
  
}
