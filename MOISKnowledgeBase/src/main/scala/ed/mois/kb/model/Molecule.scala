/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Molecule (
  val parent_ptr_species_component_id: Option[Int]) {

  def this() = this(Some(0))
    
   
}