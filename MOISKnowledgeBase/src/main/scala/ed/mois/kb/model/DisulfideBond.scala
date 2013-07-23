/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_DisulfideBond (
  val protein_monomer_id: Option[Int],
  val residue_1: Option[Int],
  val residue_2: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0), Some(0))
    
  lazy val evidences = KnowledgeBase.disulfidebond_evidence.left(this)   
}
        
class Public_DisulfideBond_Evidence (
  val disulfidebond_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}