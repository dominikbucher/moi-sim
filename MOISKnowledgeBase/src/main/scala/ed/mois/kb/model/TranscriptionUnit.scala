/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_TranscriptionUnit (
  val parent_ptr_molecule_id: Option[Int],
  val promoter_35_coordinate: Option[Int],
  val promoter_35_length: Option[Int],
  val promoter_10_coordinate: Option[Int],
  val promoter_10_length: Option[Int],
  val tss_coordinate: Option[Int]) {

  def this() = this(Some(0), Some(0), Some(0), Some(0), Some(0), Some(0))
    
  lazy val genes = KnowledgeBase.transcriptionunit_genes.left(this)   
}
        
class Public_TranscriptionUnit_genes (
  val transcriptionunit_id: Int,
  val gene_id: Int) 
  extends KnowledgeBaseObject {

}