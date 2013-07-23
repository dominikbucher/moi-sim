/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EntryFloatData (
  val value: Option[Double],
  val units: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(0.0), Some(""))
    
  lazy val evidences = KnowledgeBase.entryfloatdata_evidence.left(this)   
}
        
class Public_EntryFloatData_Evidence (
  val entryfloatdata_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}