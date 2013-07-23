/*
 * Contains tests for the simulator.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test

import org.scalatest.FlatSpec
import ed.mois.core.comm.ChangeRequest
import ed.mois.core.sim.chg.Atomic
import ed.mois.core.sim.chg.DoubleChange
import ed.mois.core.sim.prop.PropertyId
import ed.mois.core.sim.Simulator
import ed.mois.core.sim.integr.StepbackIntegrator
import ed.mois.core.math.AnyMatrix
import ed.mois.core.storm._

class SimTest extends FlatSpec {
  "A stepback integrator" should "sort changes by property" in {
    val integr = new StepbackIntegrator(null)
    val chgR = List(
      ChangeRequest("p1",
        List(Atomic("p1", 0.0,
          List(DoubleChange(PropertyId("s1", "prop1", 0), 234.0),
            DoubleChange(PropertyId("s1", "prop2", 1), 454.0))),
          Atomic("p2", 0.0,
            List(DoubleChange(PropertyId("s1", "prop1", 0), 10.0),
              DoubleChange(PropertyId("s1", "prop2", 1), 20.0))))))
    val sorted = integr.sortByProperty(chgR)
    assert(sorted(0)(1).propertyChanges.filter(_.value == 234.0).length === 1)
    assert(sorted(0)(0).propertyChanges.filter(_.value == 10.0).length === 1)
    assert(sorted(1)(1).propertyChanges.filter(_.value == 454.0).length === 1)
    assert(sorted(1)(0).propertyChanges.filter(_.value == 20.0).length === 1)
  }

  "AnyMatrix" should "remove rows nicely" in {
    var matr = new AnyMatrix(5, 5)
    for {
      i <- 0 until matr.nRows
      j <- 0 until matr.nCols
    } {
      matr(i, j) = i + j
    }
    println(matr.toString)
    matr.fields = matr.removeRows(List(2, 3))
    println(matr.toString)
  }

  "AnyMatrix" should "remove columns nicely" in {
    var matr = new AnyMatrix(5, 5)
    for {
      i <- 0 until matr.nRows
      j <- 0 until matr.nCols
    } {
      matr(i, j) = i + j
    }
    println(matr.toString)
    matr.fields = matr.removeCols(List(2, 3))
    println(matr.toString)
  }
}