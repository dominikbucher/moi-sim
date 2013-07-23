/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ReactionStoichiometryParticipant (
  val molecule_id: Option[Int],
  val coefficient: Option[Double],
  val compartment_id: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0.0), Some(0))
    
  lazy val evidences = KnowledgeBase.reactionstoichiometryparticipant_evidence.left(this)   
}
        
class Public_ReactionStoichiometryParticipant_Evidence (
  val reactionstoichiometryparticipant_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}