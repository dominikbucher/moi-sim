/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_SpeciesComponent (
  val parent_ptr_entry_id: Option[Int],
  val species_id: Option[Int]) {

  def this() = this(Some(0), Some(0))
    
  lazy val ttype = KnowledgeBase.speciescomponent_type.left(this)
  lazy val references = KnowledgeBase.speciescomponent_references.left(this)   
}
        
class Public_SpeciesComponent_type (
  val speciescomponent_id: Int,
  val type_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_SpeciesComponent_references (
  val speciescomponent_id: Int,
  val reference_id: Int) 
  extends KnowledgeBaseObject {

}