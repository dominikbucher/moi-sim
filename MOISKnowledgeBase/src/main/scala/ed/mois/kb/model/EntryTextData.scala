/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EntryTextData (
  val value: Option[String],
  val units: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""))
    
  lazy val evidences = KnowledgeBase.entrytextdata_evidence.left(this)   
}
        
class Public_EntryTextData_Evidence (
  val entrytextdata_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}