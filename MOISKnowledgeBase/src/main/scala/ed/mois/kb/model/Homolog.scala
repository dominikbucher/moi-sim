/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Homolog (
  val xid: Option[String],
  val species: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""))
    
  lazy val evidences = KnowledgeBase.homolog_evidence.left(this)   
}
        
class Public_Homolog_Evidence (
  val homolog_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}