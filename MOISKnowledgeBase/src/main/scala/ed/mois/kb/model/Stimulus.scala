/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Stimulus (
  val parent_ptr_molecule_id: Option[Int],
  val value_id: Option[Int]) {

  def this() = this(Some(0), Some(0))
    
   
}