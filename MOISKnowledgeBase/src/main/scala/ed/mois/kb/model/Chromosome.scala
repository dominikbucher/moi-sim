/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Chromosome (
  val parent_ptr_molecule_id: Option[Int],
  val sequence: Option[String],
  val length: Option[Int]) {

  def this() = this(Some(0), Some(""), Some(0))
    
   
}