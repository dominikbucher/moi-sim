/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Reference (
  val parent_ptr_species_component_id: Option[Int],
  val authors: Option[String],
  val editors: Option[String],
  val year: Option[Int],
  val title: Option[String],
  val publication: Option[String],
  val publisher: Option[String],
  val volume: Option[String],
  val issue: Option[String],
  val pages: Option[String]) {

  def this() = this(Some(0), Some(""), Some(""), Some(0), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))
    
   
}