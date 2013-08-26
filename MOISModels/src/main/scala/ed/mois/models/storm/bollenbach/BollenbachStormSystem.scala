/*
 * Contains an implementation of the system by Tobias Bollenbach running on the storm simulator.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.models.storm.bollenbach

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.immutable.TreeMap

import ed.mois.core.storm._
import ed.mois.core.storm.strategies._
import ed.mois.core.util.plot.s4gnuplot.Gnuplot

object BollenbachSystemRunner extends App {
  var g_max = 0.0
  var sr_max = 0.0
  for (i <- 9000 to 10000) {
    val sim = new StormSim {
      override val simulationStrategy = () => new SmashStrategy(16.0, 0.01) {override val debug = false}
      val model = new BollenbachModel(i)
    }

    val results = sim.runSim
    val res = Await.result(results, 60 seconds)

    if (res.last._2.asInstanceOf[BollenbachState].g() > g_max) {
      g_max = res.last._2.asInstanceOf[BollenbachState].g()
      sr_max = i
    }
    //println(s"$i: $g_max")
  }
  println(s"g_max: $g_max, sr_max: $sr_max")
}

case class BollenbachState extends StormState[BollenbachState] {
  import Constants._
  var p = field(_p) >= 0.0
  var c = field(_c) >= 0.0
  var r = field(_r) >= 0.0
  var a = field(_a) >= 0.0
  var g = field(_g) 

  var r_real = field(0.0)
  var a_real = field(0.0) // scaled version of a (as a is arbitrary anyways)
}

/**
 * The Bollenbach system implements the simple mathematical model
 * used by Bollenbach et al. to simulate cell behavior.
 */
class BollenbachModel(iSr: Double) extends StormModel {
  type StateType = BollenbachState

  import Constants._
  import Functions._

  lazy val stateVector = BollenbachState()
  addProcess(() => new Metabolism)
  // lazy val processes: Array[() => StormProcess[BollenbachState]] = Array(
  //   () => new Metabolism)

  val title = "Bollenbach Ribosome Inhibitor Model"
  val desc = "The model used in the paper 'Nonoptimal Microbial Response to Antibiotics Underlies Suppressive Drug Interactions."
  val authors = "Tobias Bollenbach"
  val contributors = "Dominik Bucher"

  override val observables = {
    import stateVector._
    List()
  }

  def calcDependencies(state: BollenbachState) = {
    import state._
    r_real() = r() / pr
    a_real() = a() * 40.0
  }

  class Metabolism extends StormProcess[BollenbachState] {
    def name = "Metabolism"
    def evolve(state: BollenbachState, t: Double, dt: Double) = {
      import state._

      val V = kV * (p() + pr * r())
      //val eta = (pr*self.sr) / (self.sp + pr*self.sr)
      val fres = (a() / V) / (Ma + a() / V)
      val TC =  tauC0 / fres
      val TD =  tauD0 / fres
      val No = math.exp(g() * (TC + TD))
      val Nf = 2 * math.exp(g() * TD) * (-1 + math.exp(g() * TC))
      //println(s"$fres, $TC, $TD, $No, $Nf")

      val sr = math.min(iSr, sr0 * fres * Nrrn * No)
      
      //val sr = Nrrn * No *  sr0 * f_fres(a(), V) //iSr // Nrrn * No * math.min(sr0 /*opt??*/ , sr0 * f_fres(a(), f_V(r(), p())))
      val sp = kp0 * fres * r() - pr * sr //(1.0 - eta) * kp0 * f_fres(a(), V) * rho * r() // f_spec_sp(a(), r(), p(), sr)
      val sc = Nf * 1.0 / (2.0 * TC)
      val sa = _va
      // val sr = Nrrn * f_No(a(), r(), p(), g()) *  sr0 * f_fres(a(), f_V(r(), p())) //iSr // Nrrn * No * math.min(sr0 /*opt??*/ , sr0 * f_fres(a(), f_V(r(), p())))
      // val sp = (1.0 - f_eta(r(), p())) * kp0 * f_fres(a(), f_V(r(), p())) * rho * r() // f_spec_sp(a(), r(), p(), sr)
      // val sc = f_Nf(a(), r(), p(), g()) * 1.0 / (2.0 * f_tauC(a(), r(), p()))
      // val sa = va

      //println(s"$t: ${p()}, ${c()}, ${r()}, ${a()}, ${g()}, $sr, $sp, $sc, $sa")
//      state.print
//      println

      //g() = f_spec_g(sp, a(), r(), p(), g())
      g() = sp / (No * po)

      p() += (sp - g() * p()) * dt
      c() += (sc - g() * c()) * dt
      r() += (sr - g() * r()) * dt
      a() += (sa - (g() + kdeg) * a() - (epsP * sp + epsR * sr + epsC * sc)) * dt
    }
  }
}

object Constants {
  val kdeg = 0.12 // h^-1; Resource degradation rate
  val epsP = 8.1 * 0.0000001 // Resources consumed to make one protein
  val epsR = 2.2 * 0.00001 // Resource consumed to make one ribosome
  val epsC = 0.039 // Resources consumed to make one chromosome

  val Ma = 0.53 // um^-3; Resource conc. where chain elongation rates are at half max
  val po = 9.9 * 100000.0 // Protein per replication origin
  val pr = 20.7 // Amount of protein per ribosome
  val kV = 3.73 * 0.0000001 // um^3; Cell volume per protein
  val kp0  = 0.059  * 3600
  val sr0 = 72.0 * 60.0
  val tauC0 = 33 / 60.0
  val tauD0 = 16 / 60.0
  //val kp0 = 0.059 // s^-1; Maximal rate of protein synthesis per ribosome
  // val sr0 = 72.0 // min; Maximal rate of ribosome synthesis per rrn operon
  // val tauC0 = 33.0 / 60.0// min; Minimal replication time of chromosome
  // val tauD0 = 16.0 // min; Minimal delay before cell division
  val Nrrn = 7.0 // 1.0 to 7.0; Number of rrn operons per chromosome
  val rho = 1.0 // 0.0 to 1.0; Fraction of functional ribosomes (< 1.0 with antibiotic)
  val delta = 1.0 // 0.0 to 1.0; Relative change of DNA synthesis rate (< 1.0 with antibiotic)

  // Dependent properties ------------------------------------------------------------
  // Initial numbers
  val _g = 1.0 //0.69 // h^-1; Cell division rate, growth rate
  val _r = 1.35 * 10000.0 // # ribosomes per cell
  val _p = 2.4 * 1000000.0 // # proteins per cell
  val _c = 3.0 //1.8 // Genome equivalents of DNA per cell
  val _a = 1.0 // # of resources per cell (called atp here); arbitrarily chosen

  val kp = 0.038 // s^-1; Rate of protein synthesis per ribosome
  //val tauC = Functions.f_tauC(_a, _r, _p) // 50.0 // min; Replication time of chromosome
  //val tauD = Functions.f_tauD(_a, _r, _p) // 25.0 // min; Delay before cell division
  //val Nf = Functions.f_Nf(_a, _r, _p, _g) // 2.1 // # of replication forks
  //val No = Functions.f_No(_a, _r, _p, _g) // 2.4 // # of replication origins
  //val V = Functions.f_V(_r, _p) // 1.0 // um^3; Cell volume
  val _va = 2.42 // h^-1; Resource influx
  // val _sr = Nrrn *No *  sr0 * Functions.f_fres(_a, V)
  // val _sp = po * _g * No
  //val eta = Functions.f_eta(sr, sp) //pr * _sr / (_sp + pr * _sr) // 0.11 // Ribosomal protein fraction (0 < eta < 1)
  //println(s"tauC: $tauC, tauD: $tauD, Nf: $Nf, No: $No, V: $V, eta: $eta")
}

object Functions {
  import Constants._

  def f_V(r: Double, p: Double) = kV * (p + pr * r)
  def f_eta(sr: Double, sp: Double) = pr * sr / (sp + pr * sr)
  //def f_eta(sr: Double, sp: Double) = pr * sr / (sp + pr * sr)
  def f_fres(a: Double, V: Double) = (a / V) / (Ma + a / V)
  def f_No(a: Double, r: Double, p: Double, g: Double) = math.pow(math.E,
    g * (f_tauD(a, r, p) + f_tauC(a, r, p)))
  def f_Nf(a: Double, r: Double, p: Double, g: Double) = 2.0 *
    math.pow(math.E, g * f_tauD(a, r, p)) *
    (-1.0 + math.pow(math.E, g * f_tauC(a, r, p)))
  def f_tauC(a: Double, r: Double, p: Double) = tauC0 / f_fres(a, f_V(r, p)) * delta / 60.0
  def f_tauD(a: Double, r: Double, p: Double) = tauD0 / f_fres(a, f_V(r, p)) / 60.0

  def f_sropt(a: Double, r: Double, p: Double, g: Double) = f_srmax(a) / (Nrrn * f_No(a, r, p, g))
  def f_srmax(a: Double) = 1.0
  def f_sp(a: Double, r: Double, p: Double, g: Double) = po * g * f_No(a, r, p, g)

  // Special functions that calculate things for a fixed sr
  //def f_spec_sp(a: Double, r: Double, p: Double, sr: Double) =
  //  f_fres(a, f_V(r, p)) * kp0 * r * rho - pr * sr
  // def f_spec_g(sp: Double, a: Double, r: Double, p: Double, g: Double) = sp / (po * f_No(a, r, p, g))
  //def f_spec_g(sp: Double, a: Double, r: Double, p: Double) = {
  //  numericalApprox(g => sp / (po * g) - math.pow(math.E, g * (f_tauD(a, r, p) + f_tauC(a, r, p))))
  //}

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