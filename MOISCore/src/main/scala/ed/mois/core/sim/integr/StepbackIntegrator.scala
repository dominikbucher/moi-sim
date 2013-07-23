/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.integr

import scala.Array.canBuildFrom

import ed.mois.core.comm.ChangeRequest
import ed.mois.core.sim.Simulator
import ed.mois.core.sim.chg.Atomic
import ed.mois.core.sim.chg.Change
import ed.mois.core.sim.chg.Flux
import ed.mois.core.sim.prop.Property
import ed.mois.core.sim.prop.TrackedProperty

class StepbackIntegrator(sim: Simulator) extends Integrator(sim) {
  /**
   * Sorts a list of change requests by property id's. Gives a map
   * where all associated changes with a given property are listed.
   *
   * @param changeRequests The change requests to be resorted.
   * @return A map keyed by property id's that contains all involved changes
   * 		 with a given property.
   */
  def sortByProperty(changeRequests: List[ChangeRequest]) = {
    var res = collection.mutable.Map.empty[Int, List[Change]]
    for {
      cr <- changeRequests
      c <- cr.changes
      pc <- c.propertyChanges
    } {
      val tmpChgs = res.getOrElse(pc.id.id, List.empty[Change])
      if (!tmpChgs.contains(c))
        res(pc.id.id) = c :: tmpChgs
    }
    res
  }

  def advanceSystem(chgs: List[Change], deltaT: Double): Unit = {
    println(s"advancing system by $deltaT")
    val pc = sim.propertyVectorCopy
    println(s"going from ${pc.mkString(", ")}")
    chgs.foreach(advanceSystem(pc, _, deltaT))
    println(s"to new ${pc.mkString(", ")}")
    if (violations(pc)) {
      println("violation");
    } else {
      integrateVector(pc)
    }
  }

  def integrateVector(ps: Array[Property[Any]]) {
    println(s"integrating ${ps.mkString(", ")} into ${sim.properties}")
    ps.zipWithIndex.foreach(p => sim.properties(p._2)() = p._1())
  }

  def advanceSystem(ps: Array[Property[Any]], chg: Change, deltaT: Double): Unit = {
    chg.propertyChanges.foreach(p => ps(p.id.id).applyChange(p))
  }

  def violations(ps: Array[Property[Any]]): Boolean = {
    !ps.forall(_.checkRestrictions)
  }

  def violations(chgs: collection.mutable.Map[Int, List[Change]]) = {
    chgs.map {
      c => violation(c._1, c._2)
    }
  }

  def violation(id: Int, chgs: List[Change]) = {
    val p = sim.properties(id).asInstanceOf[TrackedProperty[Any]]
    val times = chgs.sortBy(_.time).map(_.time).removeDuplicates
    var valP = p()
    var tv = -1.0
    chgs.sortBy(_.time).map { c =>
      c match {
        case c: Flux => {
          p.applyChanges(c.propertyChanges.filter(_.id.id == id), c.deltaT)
        }
        case c: Atomic => p.applyChanges(c.propertyChanges.filter(_.id.id == id), 1.0)
      }
      if (p.checkRestrictions)
        tv = c.time
    }

    p() = valP
  }

  def integrate(chgReqs: List[ChangeRequest]): Double = {
    val stepSize = sim.getParam("stepSize").getOrElse("1.0").toDouble

    val chgs = chgReqs.flatMap(_.changes)
    // Get interesting time points (either start / end of flux or atomic)
    var intrPoints: List[(Double, Change)] = chgs.flatMap { c =>
      c match {
        case cc: Flux => List((c.time -> c), (c.time + cc.deltaT) -> c)
        case cc: Atomic => List(c.time -> c)
      }
    }.sortWith((a, b) => a._1 < b._1)
    
    // Take first interesting time point
    val fIp = intrPoints.head._1
    val firstChanges = intrPoints.takeWhile(_._1 == fIp).map(_._2)
    println("intr Points: " + intrPoints.map(_._1).removeDuplicates + ", taking first one: " + fIp)
    intrPoints = intrPoints.drop(firstChanges.length)
    // Advance system to this point
    advanceSystem(chgs, intrPoints.headOption.getOrElse(1.0, null)._1 - fIp)
    intrPoints.headOption.getOrElse(stepSize, null)._1 - fIp

    // val chgsByP = sortByProperty(chgs)

    //val tv = violations(chgsByP)

  }
}