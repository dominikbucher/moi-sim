/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Parameter (
  val parent_ptr_species_component_id: Option[Int],
  val value_id: Option[Int],
  val state_id: Option[Int],
  val process_id: Option[Int]) {

  def this() = this(Some(0), Some(0), Some(0), Some(0))
    
  lazy val reactions = KnowledgeBase.parameter_reactions.left(this)
  lazy val molecules = KnowledgeBase.parameter_molecules.left(this)   
}
        
class Public_Parameter_reactions (
  val parameter_id: Int,
  val reaction_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Parameter_molecules (
  val parameter_id: Int,
  val molecule_id: Int) 
  extends KnowledgeBaseObject {

}