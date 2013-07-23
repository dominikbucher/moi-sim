/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ReactionMapCoordinate (
  val path: Option[String],
  val value_x: Option[Double],
  val value_y: Option[Double],
  val label_x: Option[Double],
  val label_y: Option[Double])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(0.0), Some(0.0), Some(0.0), Some(0.0))
    
   
}