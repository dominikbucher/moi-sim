/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_CoenzymeParticipant (
  val metabolite_id: Option[Int],
  val compartment_id: Option[Int],
  val coefficient: Option[Double])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0), Some(0.0))
    
  lazy val evidences = KnowledgeBase.coenzymeparticipant_evidence.left(this)   
}
        
class Public_CoenzymeParticipant_Evidence (
  val coenzymeparticipant_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}