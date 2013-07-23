/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
import org.squeryl.annotations.Column
    
class Public_SignalSequence (
  @Column("type")
  val ttype: Option[String],
  val location: Option[String],
  val length: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""), Some(0))
    
  lazy val evidences = KnowledgeBase.signalsequence_evidence.left(this)   
}
        
class Public_SignalSequence_Evidence (
  val signalsequence_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}