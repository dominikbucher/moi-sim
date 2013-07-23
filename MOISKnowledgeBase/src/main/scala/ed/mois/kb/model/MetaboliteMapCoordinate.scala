/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_MetaboliteMapCoordinate (
  val compartment_id: Option[Int],
  val x: Option[Double],
  val y: Option[Double])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0.0), Some(0.0))
    
   
}