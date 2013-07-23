/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Evidence (
  val value: Option[String],
  val units: Option[String],
  val is_experimentally_constrained: Option[Boolean],
  val species: Option[String],
  val media: Option[String],
  val pH: Option[Double],
  val temperature: Option[Double],
  val comments: Option[String],
  val species_component_id: Option[Int])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""), Some(false), Some(""), Some(""), Some(0.0), Some(0.0), Some(""), Some(0))
    
  lazy val references = KnowledgeBase.evidence_references.left(this)   
}
        
class Public_Evidence_references (
  val evidence_id: Int,
  val reference_id: Int) 
  extends KnowledgeBaseObject {

}