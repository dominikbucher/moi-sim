/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_EvidencedEntryData (
)
  extends KnowledgeBaseObject {
    
  lazy val evidence = KnowledgeBase.evidencedentrydata_evidence.left(this)   
}
        
class Public_EvidencedEntryData_evidence (
  val evidencedentrydata_id: Int,
  val evidence_id: Int) 
  extends KnowledgeBaseObject {

}