/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.kb

import ed.mois.kb.LoadKnowledgeBase

/**
 * Trait to facilitate knowledge base access.
 */
trait KBAccess {
  def fromKbByName[A](name: String)(implicit m: Manifest[A]): A = LoadKnowledgeBase.fromKbByName[A](name: String)
  def fromKbByWid[A](name: String)(implicit m: Manifest[A]): A = LoadKnowledgeBase.fromKbByWid[A](name: String)
  def countObjectsByModelType(modelType: String): Int = LoadKnowledgeBase.countObjectsByModelType(modelType)
  def getReactionsOfProcess(wid: String) = LoadKnowledgeBase.getReactionsOfProcess(wid)
  def getMetabolites() = LoadKnowledgeBase.getMetabolites()
  def getMetaboliteBiomassCompositions() = LoadKnowledgeBase.getMetaboliteBiomassCompositions

  def compartmentIdToIdx(id: Int) = {
    id match {
      case 3377 => 0
      case 3378 => 1
      case 3379 => 2
      case 3380 => 3
      case 3381 => 4
      case 3382 => 5
      case _ => throw new Exception("Unknown compartment: " + id)
    }
  }
}