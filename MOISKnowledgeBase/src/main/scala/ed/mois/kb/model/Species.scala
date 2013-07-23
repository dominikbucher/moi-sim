/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Species (
  val parent_ptr_entry_id: Option[Int],
  val genetic_code: Option[String]) {

  def this() = this(Some(0), Some(""))
    
   
}