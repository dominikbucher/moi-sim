/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Process (
  val parent_ptr_species_component_id: Option[Int],
  val initialization_order: Option[Int]) {

  def this() = this(Some(0), Some(0))
    
   
}