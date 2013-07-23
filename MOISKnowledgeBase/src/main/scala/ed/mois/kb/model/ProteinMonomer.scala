/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ProteinMonomer (
  val parent_ptr_protein_id: Option[Int],
  val gene_id: Option[Int],
  val is_n_terminal_methionine_cleaved_id: Option[Int],
  val localization_id: Option[Int],
  val signal_sequence_id: Option[Int]) {

  def this() = this(Some(0), Some(0), Some(0), Some(0), Some(0))
    
   
}