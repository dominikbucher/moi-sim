/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Gene (
  val parent_ptr_molecule_id: Option[Int],
  val symbol: Option[String],
  val chromosome_id: Option[Int],
  val coordinate: Option[Int],
  val length: Option[Int],
  val direction: Option[String],
  val is_essential_id: Option[Int],
  val expression_id: Option[Int],
  val half_life_id: Option[Int],
  val amino_acid_id: Option[Int]) {

  def this() = this(Some(0), Some(""), Some(0), Some(0), Some(0), Some(""), Some(0), Some(0), Some(0), Some(0))
    
  lazy val codons = KnowledgeBase.gene_codons.left(this)
  lazy val homologs = KnowledgeBase.gene_homologs.left(this)   
}
        
class Public_Gene_codons (
  val gene_id: Int,
  val codon_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_Gene_homologs (
  val gene_id: Int,
  val homolog_id: Int) 
  extends KnowledgeBaseObject {

}