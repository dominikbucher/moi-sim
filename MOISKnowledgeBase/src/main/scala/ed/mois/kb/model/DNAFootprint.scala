/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_DNAFootprint (
  val length: Option[Int],
  val binding: Option[String],
  val region: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(""), Some(""))
    
  lazy val evidences = KnowledgeBase.dnafootprint_evidence.left(this)   
}
        
class Public_DNAFootprint_Evidence (
  val dnafootprint_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}