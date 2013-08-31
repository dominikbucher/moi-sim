/*
 * Contains a Karrlike system to show some storm simulator aspects.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.models.storm.karrlike

import ed.mois.core.storm.strategies._
import ed.mois.core.storm._

/**
 * Model that describes an (empty) large model, comparable to the Karr model (though the Karr model 
 * uses even more variables, lots of which are not really necessary though).
 */
class KarrlikeModel extends StormModel {
  type StateType = KarrlikeState

  val title = "A Karrlike System Modifying Several Things"
  val desc = "A Karrlike System to show properties of the simulator."
  val authors = "Dominik Bucher"
  val contributors = "Dominik Bucher"

  lazy val stateVector = KarrlikeState()
  ++(() => new P1)
  ++(() => new P2)
  ++(() => new P3)
  ++(() => new P4)
  ++(() => new P5)
  ++(() => new P6)
  ++(() => new P7)
  ++(() => new P8)
  ++(() => new P9)
  ++(() => new P10)
  ++(() => new P11)
  ++(() => new P12)
  ++(() => new P13)
  ++(() => new P14)
  ++(() => new P15)
  ++(() => new P16)
  ++(() => new P17)
  ++(() => new P18)
  ++(() => new P19)
  ++(() => new P20)
  ++(() => new P21)
  ++(() => new P22)
  ++(() => new P23)
  ++(() => new P24)
  ++(() => new P25)
  ++(() => new P26)

  /*lazy val processes = Array(
    () => new P1, 
    () => new P2, 
    () => new P3, 
    () => new P4, 
    () => new P5, 
    () => new P6)*/

  import stateVector._
  override val observables = List(r1, r2)
  def calcDependencies(st: KarrlikeState) = {}
}