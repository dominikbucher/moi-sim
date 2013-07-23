/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_UserProfile (
  val user_id: Option[Int],
  val affiliation: Option[String],
  val website: Option[String],
  val phone: Option[String],
  val address: Option[String],
  val city: Option[String],
  val state: Option[String],
  val zip: Option[String],
  val country: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(0), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))
    
   
}