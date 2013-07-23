/*
 * Contains tiny smash fit strategy.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.tiny.strategies

import scala.util.Random
import ed.mois.core.tiny._
import ed.mois.core.util.Refl
import ed.mois.core.math._
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import com.joptimizer.optimizers.JOptimizer
import com.joptimizer.functions.LinearMultivariateRealFunction
import com.joptimizer.optimizers.OptimizationRequest
import com.joptimizer.functions.ConvexMultivariateRealFunction

/**
 * Strategy that mashes everything by adding additive resources and
 * randomly choosing between processes otherwise. There is no notion of
 * connected changes in this case, each change on a variable is to be
 * seen as completely independent (as in contrast to connected changes
 * where A only happens if B is also admitted).
 */
class SmashFitStrategy[T <: StateVector[T]](maxTime: Double, dt: Double) extends SimulationStrategy[T] {
  // For random decisions between processes.
  val rand = new Random(System.currentTimeMillis());
  val EPSILON_OPT_TOLERANCE_FEAS = 0.1
  val EPSILON_OPT_TOLERANCE = 0.1

  def simulate(states: T, processes: Array[TinyProcess[T]], tiny: Tiny[T]): Map[Double, T] = {
    val m = collection.mutable.Map.empty[Double, T]
    //println(printStatesHeader(states, 6))

    for (t <- 0 to (maxTime / dt).toInt) {
      val time = t.toDouble * dt
      tiny.calcDependencies(states)
      m += (time -> states.dupl)
      //println(printStates(t.toDouble * dt, states, 6))
      val results = processes.map(_.evolve(states.dupl, time, dt)) //.map(Refl.getCCParams(_))
      //results.foreach(r => println(r.fields))
      states.fields = merge(states, results)
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
  def merge(init: T, newS: ArraySeq[T]): collection.mutable.Map[Int, Any] = {
    // Create new matrix that stores new state of each process
    val matr = new AnyMatrix(newS.head.fields.size, newS.length)
    // Fill matrix
    for {
      s <- newS.zipWithIndex
      f <- s._1.fields
    } {
      matr(f._1, s._2) = f._2
    }
    // Initialize t = 0.0 at beginning of sim step
    var t = 0.0
    // Assign temporary flux matrix that is updated each iteration
    var phiN = matr
    // Iterate until whole time step is resolved
    while (t < dt) {
      // List of unimportant indexes in flux matrix
      var nonImportantIdx = List.empty[Int]
      // Create matrix to be optimized
      var x = Mat.fromValue(newS.length, 1, 1.0)
      // Calculate difference up until dt (max time the flux has to be applied)
      var tv = dt - t
      // Numer of violations in remaining tv
      var nViols = 0
      // Calculate violations - non-violating resources are thrown out of the matrix to be optimized
      for (f <- init.fields) {
        val v = violation(f, newS, tv)
        if (v.isEmpty) {
          nonImportantIdx = f._1 :: nonImportantIdx
        } else {
          tv = math.min(tv, v.get)
          nViols += 1
        }
      }
      // Advance system as far as possible (up until next violation)
      advanceSystem(tv, phiN, init.fields)
      // Adjust matrix to be optimized
      phiN.fields = matr.removeRows(nonImportantIdx)
      t = t + tv
      // If there were any violations, recalculate phi
      if (nViols > 0) {
        //phiN.fields = linearProgResolveIssues(newS.length, nonImportantIdx, new Mat(matr.to2DArray[Double])).sm
      }
    }
    init.fields
  }

  /**
   * The violation function determines the first violation that happens in a
   * particular resource.
   */
  def violation(f: Tuple2[Int, Any], newS: ArraySeq[T], dt: Double): Option[Double] = {
    f._2 match {
      case d: Double => {
        val tot = newS.foldLeft(0.0)((b, t) => b + (t.fields(f._1).asInstanceOf[Double] - d))
        val tv = -d / tot
        if (0.0 <= tv && tv <= dt)
          Some(tv)
        else None
      }
      case o => {
        None
        /*val chgs = newS.filter(!_.equals(o))
        if (chgs.length > 1) {
          val ri = rand.nextInt(chgs.length); chgs(ri)
        } else if (chgs.length == 1) {
          chgs(0)
        } else {
          o
        }*/
      }
    }
  }

  def advanceSystem(dt: Double, fluxMatrix: AnyMatrix, fields: collection.mutable.Map[Int, Any]) {
    for (i <- 0 until fluxMatrix.nRows) {
      fields(i) = fields(i).asInstanceOf[Double] +
        fluxMatrix(i).foldLeft(0.0)((b, t) =>
          b + (t.asInstanceOf[Double] - fields(i).asInstanceOf[Double])) * dt
    }
  }

  def resolveIssues(idx: Int, fluxMatrix: Mat) = {
    var posFlux = 0.0
    var negFlux = 0.0
    for (i <- 0 until fluxMatrix.nCols) {
      if (fluxMatrix(idx, i) >= 0.0) posFlux += fluxMatrix(idx, i)
      else negFlux += fluxMatrix(idx, i)
    }
    val scale = posFlux / Math.abs(negFlux)
    val m = Mat(fluxMatrix.nRows, fluxMatrix.nCols)
    for (i <- 0 until fluxMatrix.nCols) {
      if (fluxMatrix(idx, i) < 0.0) {
        for (j <- 0 until fluxMatrix.nRows) {
          m(j, i) = fluxMatrix(j, i) * scale
        }
      } else {
        for (j <- 0 until fluxMatrix.nRows) {
          m(j, i) = fluxMatrix(j, i)
        }
      }
    }
    m
  }

  def linearProgResolveIssues(nProcs: Int, idxs: List[Int], fluxMatrix: Mat) = {
    //println("linearProgResolveIssues")
    def identityMatrix(n: Int) = Array.tabulate(n, n)((x, y) => if (x == y) 1.0 else 0.0)
    def printMatrix[T](m: Array[Array[T]]) = m map (_.mkString("[", ", ", "]")) mkString "\n"
    def printArray[T](m: Array[T]) = m.mkString("[", ", ", "]")

    // Objective function (plane)
    val objectiveFunction: LinearMultivariateRealFunction =
      new LinearMultivariateRealFunction(Array.fill(nProcs)(-1.0), 0)

    //inequalities (polyhedral feasible set G.X<H )
    val inequalities: Array[ConvexMultivariateRealFunction] = Array.ofDim[ConvexMultivariateRealFunction](nProcs)
    val G: Array[Array[Double]] = identityMatrix(nProcs)
    val H: Array[Double] = Array.fill(nProcs)(1.0)
    for (i <- 0 until inequalities.length) {
      inequalities(i) = new LinearMultivariateRealFunction(G(i), -H(i))
    }

    //    def rowExists(arr: Array[Array[Double]], maxArrRow: Int, mat: Mat, r: Int): Boolean = {
    //      var exists = false
    //      for (arrRow <- 0 to maxArrRow) {
    //        var tmpExists = true
    //        for (i <- 0 until mat.nCols) {
    //          if (arr(arrRow)(i) != mat(r, i)) tmpExists = false
    //        }
    //        exists = exists || tmpExists
    //      }
    //
    //      exists
    //    }

    val A: Array[Array[Double]] = Array.ofDim(idxs.length)
    var arrIdx = 0
    for (matIdx <- idxs) {
      A(arrIdx) = Array.ofDim(nProcs)
      //      if (!rowExists(A, arrIdx, fluxMatrix, matIdx)) {
      for (j <- 0 until A(arrIdx).length) {
        A(arrIdx)(j) = fluxMatrix(matIdx, j)
      }
      arrIdx += 1
      //      }
    }
    //    A = A.slice(0, arrIdx)
    //println(s"Matrix A=${printMatrix(A)}")
    val b: Array[Double] = Array.fill(idxs.length)(0.0)

    //optimization problem
    val or: OptimizationRequest = new OptimizationRequest()
    or.setF0(objectiveFunction)
    or.setFi(inequalities)
    //or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
    or.setA(A)
    or.setB(b)
    or.setToleranceFeas(EPSILON_OPT_TOLERANCE_FEAS)
    or.setTolerance(EPSILON_OPT_TOLERANCE)

    //optimization
    val opt: JOptimizer = new JOptimizer()
    opt.setOptimizationRequest(or)
    val sol = try {
      val returnCode = opt.optimize
      opt.getOptimizationResponse.getSolution
    } catch {
      case e: Exception => {
        // TODO Is this correct? Is it true that when the matrix doesn't have full rank, there is no solution?
        // Block all non-zero consuming reactions in the problematic fluxes
        val tmpSol = Array.fill(nProcs)(1.0)
        for {
          i <- 0 until A.length
          j <- 0 until A(i).length
        } {
          if (A(i)(j) < 0.0) tmpSol(j) = 0.0
        }
        tmpSol
      }
    }

    //println(s"Solution A=${printArray(sol)}")

    for {
      i <- 0 until fluxMatrix.nRows
      j <- 0 until fluxMatrix.nCols
    } {
      fluxMatrix(i, j) = fluxMatrix(i, j) * sol(j)
    }
    fluxMatrix
  }
}