/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_TranscriptionalRegulation (
  val parent_ptr_species_component_id: Option[Int],
  val transcription_unit_id: Option[Int],
  val transcription_factor_id: Option[Int],
  val binding_site_id: Option[Int],
  val affinity_id: Option[Int],
  val activity_id: Option[Int]) {

  def this() = this(Some(0), Some(0), Some(0), Some(0), Some(0), Some(0))
    
   
}