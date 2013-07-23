/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ProteinComplexBiosythesisParticipant (
  val molecule_id: Option[Int],
  val residue: Option[Int],
  val coefficient: Option[Double],
  val compartment_id: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0), Some(0.0), Some(0))
    
  lazy val evidences = KnowledgeBase.proteincomplexbiosythesisparticipant_evidence.left(this)   
}
        
class Public_ProteinComplexBiosythesisParticipant_Evidence (
  val proteincomplexbiosythesisparticipant_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}