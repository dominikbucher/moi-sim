/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Reaction (
  val parent_ptr_species_component_id: Option[Int],
  val direction: Option[String],
  val modification_id: Option[Int],
  val enzyme_id: Option[Int],
  val is_spontaneous: Option[Boolean],
  val delta_g: Option[Double],
  val keq_id: Option[Int],
  val kinetics_forward_id: Option[Int],
  val kinetics_backward_id: Option[Int],
  val optimal_ph_id: Option[Int],
  val optimal_temperature_id: Option[Int],
  val processes_id: Option[Int],
  val states_id: Option[Int]) {

  def this() = this(Some(0), Some(""), Some(0), Some(0), Some(false), Some(0.0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0))
    
  lazy val stoichiometry = KnowledgeBase.reaction_stoichiometry.left(this)
  lazy val coenzymes = KnowledgeBase.reaction_coenzymes.left(this)
  lazy val pathways = KnowledgeBase.reaction_pathways.left(this)
  lazy val map_coordinates = KnowledgeBase.reaction_map_coordinates.left(this) 
}
        
class Public_Reaction_stoichiometry (
  val reaction_id: Int,
  val reactionstoichiometryparticipant_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Reaction_coenzymes (
  val reaction_id: Int,
  val coenzymeparticipant_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Reaction_pathways (
  val reaction_id: Int,
  val pathway_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Reaction_map_coordinates (
  val reaction_id: Int,
  val reactionmapcoordinate_id: Int) 
  extends KnowledgeBaseObject {

}