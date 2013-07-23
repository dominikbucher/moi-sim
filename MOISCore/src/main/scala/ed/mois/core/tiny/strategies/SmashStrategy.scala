/*
 * Contains tiny smash strategy.
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
class SmashStrategy[T <: StateVector[T]](maxTime: Double, dt: Double) extends SimulationStrategy[T] {
  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());

  def simulate(states: T, processes: Array[TinyProcess[T]], tiny: Tiny[T]): Map[Double, T] = {
    val m = collection.mutable.Map.empty[Double, T]
    //println(printStatesHeader(states, 6))
    
    for (t <- 0 to (maxTime / dt).toInt) {
      val time = t.toDouble * dt
      tiny.calcDependencies(states)
      m += (time -> states.dupl)
      //println(printStates(t.toDouble * dt, states, 6))
      val results = processes.map(_.evolve(states.dupl, time, dt))//.map(Refl.getCCParams(_))
      //results.foreach(r => println(r.fields))
      for (f <- states.fields) {
        states.fields(f._1) = mash(f._2, results.map(_.fields(f._1)).toArray)
      }
    }
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
  def mash(init: Any, newS: Array[Any]): Any = {
    init match {
      case d: Double => newS.foldLeft(d)((b, t) => b + (t.asInstanceOf[Double] - d))
      case o => {
        val chgs = newS.filter(!_.equals(o))
        if (chgs.length > 1) {
          val ri = rand.nextInt(chgs.length); chgs(ri)
        } else if (chgs.length == 1) {
          chgs(0)
        } else {
          o
        }
      }
    }
  }
}