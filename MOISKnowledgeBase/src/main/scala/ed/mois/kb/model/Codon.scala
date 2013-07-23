/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Codon (
  val sequence: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""))
    
  lazy val evidences = KnowledgeBase.codon_evidence.left(this)   
}
        
class Public_Codon_Evidence (
  val codon_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}