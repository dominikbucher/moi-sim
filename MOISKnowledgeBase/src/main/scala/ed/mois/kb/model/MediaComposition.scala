/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_MediaComposition (
  val concentration: Option[Double],
  val is_diffused: Option[Boolean])
  extends KnowledgeBaseObject {

  def this() = this(Some(0.0), Some(false))
    
  lazy val evidences = KnowledgeBase.mediacomposition_evidence.left(this)   
}
        
class Public_MediaComposition_Evidence (
  val mediacomposition_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}