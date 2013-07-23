/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EnzymeParticipant (
  val protein_id: Option[Int],
  val compartment_id: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0))
    
  lazy val evidences = KnowledgeBase.enzymeparticipant_evidence.left(this)   
}
        
class Public_EnzymeParticipant_Evidence (
  val enzymeparticipant_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}