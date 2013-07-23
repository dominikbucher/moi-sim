/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EntryCharData (
  val value: Option[String],
  val units: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""))
    
  lazy val evidences = KnowledgeBase.entrychardata_evidence.left(this)   
}
        
class Public_EntryCharData_Evidence (
  val entrychardata_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}