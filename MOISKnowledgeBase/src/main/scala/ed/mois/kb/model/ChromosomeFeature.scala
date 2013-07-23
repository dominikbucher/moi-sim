/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ChromosomeFeature (
  val parent_ptr_species_component_id: Option[Int],
  val chromosome_id: Option[Int],
  val coordinate: Option[Int],
  val length: Option[Int],
  val direction: Option[String]) {

  def this() = this(Some(0), Some(0), Some(0), Some(0), Some(""))
    
   
}