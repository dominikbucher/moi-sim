/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_BindingSite (
  val coordinate: Option[Int],
  val length: Option[Int],
  val direction: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(0), Some(""))
    
  lazy val evidences = KnowledgeBase.bindingsite_evidence.left(this)   
}
        
class Public_BindingSite_Evidence (
  val bindingsite_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}