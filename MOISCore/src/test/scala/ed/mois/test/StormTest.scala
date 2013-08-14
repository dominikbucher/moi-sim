/*
 * Contains tests for the storm simulator.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test

import org.scalatest.FlatSpec
import ed.mois.core.storm._

class StormTest extends FlatSpec {
  "ChangeHelper" should "Rearrange changes according to time of occurrence" in {
    val a = StormChange(0, 0.0, 1.0, Map.empty[Int, Any])
    val b = StormChange(1, 0.0, 1.0, Map.empty[Int, Any])
    val c = StormChange(2, 0.2, 0.2, Map.empty[Int, Any])
    val d = StormChange(3, 0.3, 0.5, Map.empty[Int, Any])

    val chgLst = List(a, b, c, d)
    val t = new Object with ChangeHelper

    val sliced = t.slices(chgLst)

    //assert(sliced(0.0).contains(a) && sliced(0.0).contains(b))
    //assert(sliced(0.2).contains(a) && sliced(0.2).contains(b) && sliced(0.2).contains(c))
    //assert(sliced(0.3).contains(a) && sliced(0.3).contains(b) && sliced(0.3).contains(c) && sliced(0.3).contains(d))
    println(t.slices(chgLst))
  }

  "ChangeHelper" should "Integrate changes correctly" in {
    case class St extends StormState[St] {
      var fa = field(50.0) >= 0.0
      var fb = field(50.0) >= 0.0
      var fc = field(50.0) >= 0.0
    }
    var m = collection.mutable.Map.empty[Double, St]

    val state = St()
    m += (0.0 -> state.dupl)
    println(m)

    val a = StormChange(0, 0.0, 1.0, Map(0 -> -4.0))
    val b = StormChange(1, 0.0, 1.0, Map(1 -> -12.0))
    val c = StormChange(2, 0.2, 0.2, Map(2 -> -5.0))
    val d = StormChange(3, 0.3, 0.5, Map(2 -> -8.0))

    val chgLst = List(a, b, c, d)
    val t = new Object with ChangeHelper

    val sliced = t.intersectAndStore(m, state, state.fieldPtrs, chgLst, 0.0, 1.0)

    //assert(sliced(0.0).contains(a) && sliced(0.0).contains(b))
    //assert(sliced(0.2).contains(a) && sliced(0.2).contains(b) && sliced(0.2).contains(c))
    //assert(sliced(0.3).contains(a) && sliced(0.3).contains(b) && sliced(0.3).contains(c) && sliced(0.3).contains(d))
    println(s"${state.fa}, ${state.fb}, ${state.fc}")
    println(sliced)
    println(m)
  }


}