/*
 * Contains main tiny simulator stuff.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny

import ed.mois.core.tiny.strategies.SimulationStrategy
import ed.mois.core.util.plot.s4gnuplot.Gnuplot
import scala.collection.immutable.TreeMap
import ed.mois.core.util.Refl

/**
 * Defines the outline of a tiny simulator. This simulator doesn't:
 * - run in parallel
 * - splits the state in any way
 * - defines or requires an extensive simulation graph
 * - does any specializations or optimizations
 *
 * The purpose of Tiny is mainly to implement different sharing strategies
 * fast and to test them for correctness and against each other.
 */
abstract class Tiny[T <: StateVector[T]] {
  val stateVector: T
  val processes: Array[TinyProcess[T]]

  val observables = List.empty[Field[_]]
  val title: String

  val simulationStrategy: SimulationStrategy[T]
  def calcDependencies(st: T)

  def runSim: TreeMap[Double, T] = {
    val endSim = simulationStrategy.simulate(stateVector, processes, this)

    if (!observables.isEmpty) {
      val gnuData = TreeMap(endSim.toArray: _*).map(es => Seq(es._1) ++
        observables.map(o => es._2.fields(o.id).asInstanceOf[Double])).toSeq
        
      var plot = Gnuplot.newPlot
        .data(gnuData)
        .title(title)
        .xlabel("t")
        .ylabel("s(t)")
        .gridOn
      observables.zipWithIndex.foreach {
        case (o, i) =>
          plot = plot.line(_.title(stateVector.fieldNames(o.id)).using("1:" + (i + 2)).style("pt 7 ps 0.8"))
      }
      plot.plot
    }
    
    TreeMap(endSim.toArray:_*)
  }
}