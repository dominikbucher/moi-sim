/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Protein (
  val parent_ptr_molecule_id: Option[Int],
  val dna_footprint_id: Option[Int],
  val regulatory_rule_id: Option[Int]) {

  def this() = this(Some(0), Some(0), Some(0))
    
  lazy val prosthetic_groups = KnowledgeBase.protein_prosthetic_groups.left(this)
  lazy val chaperones = KnowledgeBase.protein_chaperones.left(this)   
}
        
class Public_Protein_prosthetic_groups (
  val protein_id: Int,
  val prostheticgroupparticipant_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Protein_chaperones (
  val from_protein_id: Int,
  val to_protein_id: Int) 
  extends KnowledgeBaseObject {

}