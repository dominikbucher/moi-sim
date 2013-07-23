/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model

import java.util.Date    
import ed.mois.kb.KnowledgeBase
    
class Public_Synonym (
  val name: Option[String])
  extends KnowledgeBaseObject {

  def this() = this(Some(""))
    
   
}