/*
 * Contains tiny stepadjust strategy.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny.strategies

import ed.mois.core.tiny._
import scala.util.Random
import ed.mois.core.util.Refl

/**
 * Strategy that mashes everything by adding additive resources and
 * randomly choosing between processes otherwise. There is no notion of
 * connected changes in this case, each change on a variable is to be
 * seen as completely independent (as in contrast to connected changes
 * where A only happens if B is also admitted).
 */
class StepadjustStrategy[T <: StateVector[T]](maxTime: Double, idt: Double,
  minDt: Double, maxDt: Double) extends SimulationStrategy[T] {
  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());
  val MIN_DELTA_T = minDt //0.001
  val MAX_DELTA_T = maxDt //0.5

  def simulate(states: T, processes: Array[TinyProcess[T]], tiny: Tiny[T]): Map[Double, T] = {
    val m = collection.mutable.Map.empty[Double, T]
    println(printStatesHeader(states, 6))

    var t = 0.0
    var dt = idt
    var steps = 0
    var iterations = 0
    while (t < maxTime) {
      tiny.calcDependencies(states)
      //            println(printStates(t, states, 6))
      //            println(s"dt is $dt")
      val results = processes.map(_.evolve(states.dupl, t, dt)) //.map(Refl.getCCParams(_))
      //results.foreach(r => println(r.fields))
      val mergeRes = states.fieldPtrs.map(f => step(f._2(), f._2, results.map(_.fields(f._1)).toArray, dt))
      if (!mergeRes.exists(_._1)) {
        m += (t -> states.dupl)
        mergeRes.foreach(mr => states.fields(mr._4) = mr._3)
        t += dt
        dt = math.min(dt * 2, MAX_DELTA_T)
        steps += 1
      } else {
        dt = math.max(mergeRes.foldLeft(dt)((a, b) => math.min(a, b._2)), MIN_DELTA_T)
      }
      iterations += 1
    }
    println(s"Needed $steps steps and $iterations iterations to complete simulation")
    m.toMap
  }

  /**
   * Mash supports different basic types. Doubles are handled additively
   * (without any constraints though).
   *
   * @param init The initial value.
   * @param newS What processes would like to change it to.
   * @return A new value for init (if anyone wanted to change it).
   */
  def step(initVal: Any, init: Field[_], newS: Array[Any], dt: Double): (Boolean, Double, Any, Int) = {
    initVal match {
      case d: Double => {
        var ndt = dt
        val newVal = newS.foldLeft(d)((b, t) => b + (t.asInstanceOf[Double] - d))
        if (init.geq.isDefined && newVal < init.geq.get.asInstanceOf[Double]) {
          ndt = ((init.geq.get.asInstanceOf[Double] - initVal.asInstanceOf[Double]) *
            dt / (newVal - initVal.asInstanceOf[Double])) / 2
          println(s"Value ${init.id} = $newVal is too small, resetting dt to $ndt")
          return (true, ndt, newVal, init.id)
        }
        if (init.leq.isDefined && newVal > init.leq.get.asInstanceOf[Double]) {
          ndt = ((init.leq.get.asInstanceOf[Double] - initVal.asInstanceOf[Double]) *
            dt / (newVal - initVal.asInstanceOf[Double])) / 2
          println(s"Value ${init.id} is too big, resetting dt to $ndt")
          return (true, ndt, newVal, init.id)
        }
        (false, ndt, newVal, init.id)
      }
      case o => {
        val chgs = newS.filter(!_.equals(o))
        if (chgs.length > 1) {
          val ri = rand.nextInt(chgs.length);
          return (true, dt / 2, chgs(ri), init.id)
        } else if (chgs.length == 1) {
          return (false, dt, chgs(0), init.id)
        } else {
          return (false, dt, o, init.id)
        }
      }
    }
  }
}