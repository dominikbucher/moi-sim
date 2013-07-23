/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
import org.squeryl._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._
    
class Public_Metabolite (
  val parent_ptr_molecule_id: Option[Int],
  val traditional_name: Option[String],
  val iupac_name: Option[String],
  val empirical_formula: Option[String],
  val smiles: Option[String],
  val charge: Option[Int],
  val is_hydrophobic: Option[Boolean],
  val volume: Option[Double],
  val deltag_formation: Option[Double],
  val pka: Option[Double],
  val pi: Option[Double],
  val log_p: Option[Double],
  val log_d: Option[Double],
  val media_composition_id: Option[Int]) {

  def this() = this(Some(0), Some(""), Some(""), Some(""), Some(""), Some(0), Some(false), Some(0.0), Some(0.0), Some(0.0), Some(0.0), Some(0.0), Some(0.0), Some(0))
    
  lazy val biomass_composition = KnowledgeBase.metabolite_biomass_composition.left(this)
  lazy val map_coordinates = KnowledgeBase.metabolite_map_coordinates.left(this)   
  
  //def molecule = from(KnowledgeBase.molecule)(s => where(s.parent_ptr_species_component_id === parent_ptr_molecule_id) select(s))
}
        
class Public_Metabolite_biomass_composition (
  val metabolite_id: Int,
  val biomasscomposition_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Metabolite_map_coordinates (
  val metabolite_id: Int,
  val metabolitemapcoordinate_id: Int) 
  extends KnowledgeBaseObject {

}