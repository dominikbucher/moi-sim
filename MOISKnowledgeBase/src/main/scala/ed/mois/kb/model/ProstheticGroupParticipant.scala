/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ProstheticGroupParticipant (
  val metabolite_id: Option[Int],
  val compartment_id: Option[Int],
  val coefficient: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0), Some(0))
    
  lazy val evidences = KnowledgeBase.prostheticgroupparticipant_evidence.left(this)   
}
        
class Public_ProstheticGroupParticipant_Evidence (
  val prostheticgroupparticipant_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}