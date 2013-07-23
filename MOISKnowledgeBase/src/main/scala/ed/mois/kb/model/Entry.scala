/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Entry (
  val model_type: Option[String],
  val wid: Option[String],
  val name: Option[String],
  val comments: Option[String],
  val created_user_id: Option[Int],
  val created_date: Option[Date],
  val last_updated_user_id: Option[Int],
  val last_updated_date: Option[Date])
  extends KnowledgeBaseObject {

  def this() = this(Some(""), Some(""), Some(""), Some(""), Some(0), Some(new Date(0)), Some(0), Some(new Date(0)))
    
  lazy val synonyms = KnowledgeBase.entry_synonyms.left(this)
  lazy val cross_references = KnowledgeBase.entry_cross_references.left(this)   
}
        
class Public_Entry_synonyms (
  val entry_id: Int,
  val synonym_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Entry_cross_references (
  val entry_id: Int,
  val crossreference_id: Int) 
  extends KnowledgeBaseObject {

}