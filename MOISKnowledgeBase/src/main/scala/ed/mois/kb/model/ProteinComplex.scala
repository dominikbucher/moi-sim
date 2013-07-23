/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_ProteinComplex (
  val parent_ptr_protein_id: Option[Int],
  val formation_process_id: Option[Int]) {

  def this() = this(Some(0), Some(0))
    
  lazy val biosynthesis = KnowledgeBase.proteincomplex_biosynthesis.left(this)
  lazy val disulfide_bonds = KnowledgeBase.proteincomplex_disulfide_bonds.left(this)   
}
        
class Public_ProteinComplex_biosynthesis (
  val proteincomplex_id: Int,
  val biosynthesis_id: Int) 
  extends KnowledgeBaseObject {

}

        
class Public_ProteinComplex_disulfide_bonds (
  val proteincomplex_id: Int,
  val disulfidebond_id: Int) 
  extends KnowledgeBaseObject {

}