/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EntryBooleanData (
  val value: Option[Boolean])
  extends KnowledgeBaseObject {

  def this() = this(Some(false))
    
  lazy val evidences = KnowledgeBase.entrybooleandata_evidence.left(this)   
}
        
class Public_EntryBooleanData_Evidence (
  val entrybooleandata_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}