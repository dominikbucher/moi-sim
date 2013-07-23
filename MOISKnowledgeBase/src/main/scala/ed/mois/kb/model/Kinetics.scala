/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Kinetics (
  val rate_law: Option[String],
  val km: Option[String],
  val vmax: Option[Double],
  val vmax_unit: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""), Some(0.0), Some(""))
    
  lazy val evidences = KnowledgeBase.kinetics_evidence.left(this)   
}
        
class Public_Kinetics_Evidence (
  val kinetics_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}