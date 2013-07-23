/*
 * Contains an implementation of the system by Tobias Bollenbach running on the tiny simulator.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.tiny.bollenbach

import ed.mois.core.tiny._
import ed.mois.core.tiny.strategies.SmashStrategy
import ed.mois.core.tiny.strategies.StepadjustStrategy
import ed.mois.core.util.plot.s4gnuplot.Gnuplot
import scala.collection.immutable.TreeMap

object BollenbachSystem extends App {
  val growths = collection.mutable.Map.empty[Double, Double]
  for (i <- 1 to 100) {
    val iSr = (i.toDouble / 1000.0)
    val res = (new BollenbachSystem(iSr)).runSim
    growths += iSr -> res.last._2.g()
  }
  
  var plot = Gnuplot.newPlot
    .data(TreeMap(growths.toArray:_*).map(g => Seq(g._1) ++ Seq(g._2)).toSeq)
    .title("Growth as function of sr")
    .xlabel("t")
    .ylabel("s(t)")
    .gridOn
    .line(_.title("growth").using("1:2").style("pt 7 ps 0.8"))
    .plot
}

case class BollenbachState extends StateVector[BollenbachState] {
  var p = field(50.0) >= 0.0
  var c = field(50.0) >= 0.0
  var r = field(50.0) >= 0.0
  var a = field(50.0) >= 0.0

  var g = field(0.0)
}

/**
 * The Bollenbach system implements the simple mathematical model
 * used by Bollenbach et al. to simulate cell behavior.
 */
class BollenbachSystem(iSr: Double) extends Tiny[BollenbachState] {
  import Constants._
  import Functions._

  val MAX_TIME = 24.0
  val DELTA_TIME = 0.6
  val MIN_DELTA_TIME = 0.0005
  val MAX_DELTA_TIME = 0.05

  lazy val stateVector = BollenbachState()
  lazy val processes: Array[TinyProcess[BollenbachState]] = Array(new Metabolism)
  val title = "Bollenbach Ribosome Inhibitor Model"

  lazy val simulationStrategy =
    new StepadjustStrategy[BollenbachState](MAX_TIME, DELTA_TIME, MIN_DELTA_TIME, MAX_DELTA_TIME)
  override val observables = {
    import stateVector._
    //    List(p, c, r, a, growth)
    List()
  }

  def calcDependencies(state: BollenbachState) = {
    //        import state._
  }

  class Metabolism extends TinyProcess[BollenbachState] {
    def name = "Dilution"
    def evolve(state: BollenbachState, t: Double, dt: Double): BollenbachState = {
      import state._

      val sr = iSr // Nrrn * No * math.min(sr0 /*opt??*/ , sr0 * f_fres(a(), f_V(r(), p())))
      val sp = f_spec_sp(a(), r(), p(), sr) // (1.0 - eta) * kp0 * f_fres(a(), f_V(r(), p())) * rho * r()
      val sc = f_Nf(a(), r(), p(), g()) * 1.0 / (2.0 * f_tauC(a(), r(), p()))
      val sa = va

//      println(s"$t: $sr, $sp, $sc, $sa")
//      state.print
//      println

      g() = f_spec_g(sp, a(), r(), p())

      p() += (sp - g() * p()) * dt
      c() += (sc - g() * c()) * dt
      r() += (sr - g() * r()) * dt
      a() += (sa - (g() + kdeg) * a() - (epsP * sp + epsR * sr + epsC * sc)) * dt

      state
    }
  }
}

object Constants {
  //  val g = 0.69 // h^-1; Cell division rate, growth rate
  //  val r = 1.35 * 10000.0 // # ribosomes per cell
  //  val p = 2.4 * 1000000.0 // # proteins per cell
  //  val c = 1.8 // Genome equivalents of DNA per cell
  //  val a = 1.0 // # of resources per cell (called atp here); arbitrarily chosen
  //  val eta = 0.11 // Ribosomal protein fraction (0 < eta < 1)
  //  val kp = 0.038 // s^-1; Rate of protein synthesis per ribosome
  //  val tauC = 50.0 // min; Replication time of chromosome
  //  val tauD = 25.0 // min; Delay before cell division
  //  val Nf = 2.1 // # of replication forks
  //  val No = 2.4 // # of replication origins
  //  val V = 1.0 // um^3; Cell volume
  val va = 2.42 // h^-1; Resource influx

  val kdeg = 0.12 // h^-1; Resource degradation rate
  val epsP = 8.1 * 0.0000001 // Resources consumed to make one protein
  val epsR = 2.2 * 0.00001 // Resource consumed to make one ribosome
  val epsC = 0.039 // Resources consumed to make one chromosome

  val Ma = 0.53 // um^-3; Resource conc. where chain elongation rates are at half max
  val po = 9.9 * 100000.0 // Protein per replication origin
  val pr = 20.7 // Amount of protein per ribosome
  val kV = 3.73 * 0.0000001 // um^3; Cell volume per protein
  val kp0 = 0.059 // s^-1; Maximal rate of protein synthesis per ribosome
  val sr0 = 72.0 // min; Maximal rate of ribosome synthesis per rrn operon
  val tauC0 = 33.0 // min; Minimal replication time of chromosome
  val tauD0 = 16.0 // min; Minimal delay before cell division
  val Nrrn = 7.0 // 1.0 to 7.0; Number of rrn operons per chromosome
  val rho = 1.0 // 0.0 to 1.0; Fraction of functional ribosomes (< 1.0 with antibiotic)
  val delta = 1.0 // 0.0 to 1.0; Relative change of DNA synthesis rate (< 1.0 with antibiotic)
}

object Functions {
  import Constants._

  def f_V(r: Double, p: Double) = kV * (p + pr * r)
  def f_eta(sr: Double, sp: Double) = pr * sr / (sp + pr * sr)
  def f_fres(a: Double, V: Double) = (a / V) / (Ma + a / V)
  def f_No(a: Double, r: Double, p: Double, g: Double) = math.pow(math.E,
    g * (f_tauD(a, r, p) + f_tauC(a, r, p)))
  def f_Nf(a: Double, r: Double, p: Double, g: Double) = 2.0 *
    math.pow(math.E, g * f_tauD(a, r, p)) *
    (-1.0 + math.pow(math.E, g * f_tauC(a, r, p)))
  def f_tauC(a: Double, r: Double, p: Double) = tauC0 / f_fres(a, f_V(r, p)) * delta
  def f_tauD(a: Double, r: Double, p: Double) = tauD0 / f_fres(a, f_V(r, p))

  def f_sropt(a: Double, r: Double, p: Double, g: Double) = f_srmax(a) / (Nrrn * f_No(a, r, p, g))
  def f_srmax(a: Double) = 1.0
  def f_sp(a: Double, r: Double, p: Double, g: Double) = po * g * f_No(a, r, p, g)

  // Special functions that calculate things for a fixed sr
  def f_spec_sp(a: Double, r: Double, p: Double, sr: Double) =
    f_fres(a, f_V(r, p)) * kp0 * r * rho - pr * sr
  //def f_spec_g(sp: Double, a: Double, r: Double, p: Double, g: Double) = sp / (po * f_No(a, r, p, g))
  def f_spec_g(sp: Double, a: Double, r: Double, p: Double) = {
    numericalApprox(g => sp / (po * g) - math.pow(math.E, g * (f_tauD(a, r, p) + f_tauC(a, r, p))))
  }

  /**
   * Some stupid numerical approximation of functions.
   *
   * Function is
   * f(x) = ... = 0
   *
   * @param f The function to be approximated.
   * @return The value of the approximation.
   */
  def numericalApprox(f: Double => Double) = {
    var res = 0.0
    // Denotes left bound, right bound and error
    var i = (-10.0, 10.0, 10.0)
    var fl = f(i._1)
    var fr = f(i._2)
    while (fl * fr >= 0) {
      i = (i._1 * 2, i._2 * 2, 10.0)
      fl = f(i._1)
      fr = f(i._2)
    }
    while (math.abs(i._3) > 0.01) {
      i = approx(f, i._1, i._2)
      //println(f(i._1 + (i._2 - i._1) / 2) + "(lb: " + i._1 + ", rb: " + i._2 + ")")
    }
    res = i._1 + (i._2 - i._1) / 2

    def approx(f: Double => Double, i: Double, j: Double) = {
      val fl = f(i)
      val fm = f(i + (j - i) / 2)
      val fr = f(j)
      if (fl * fm < 0) {
        (i, i + (j - i) / 2, fm - fl)
      } else {
        (i + (j - i) / 2, j, fr - fm)
      }
    }

    res
  }
}