/*
 * Contains an implementation of the system by Tobias Bollenbach running on the storm simulator.
 * The system is extended to provide some resource disposal mechanism that is analyzed.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.models.storm.resourceprocessing

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.immutable.TreeMap

import ed.mois.core.storm._
import ed.mois.core.storm.strategies._
import ed.mois.core.util.plot.s4gnuplot.Gnuplot

/**
 * The runner is just an object that contains a main method that runs the model. 
 */
object ResourceProcessingSystemRunner extends App {
  // Define a new simulator (we use the storm simulator here)
  val sim = new StormSim {
    // Specify the model, in this case the bollenbach model defined below
    val model = new ResourceProcessingModel(0.5)
    // Override the default simulation strategy to use a smash strategy with debug output
    override val simulationStrategy = () => new DistrSimPosStepAdaptionStrategy(model, 8.0, 0.01) {
      override val debug = false
    }
  }

  // Run the simulation and store the results
  val results = sim.runSim
  // As everything is asynchronous, wait for the simulation to finish before
  // shutting down application (this is actually not needed, but it's somewhat nicer
  // when starting from the console as the console waits until everything is finished)
  Await.result(results, 60 seconds)
}


case class ResourceProcessingState extends StormState[ResourceProcessingState] {
  // Shortcut to constants
  import Constants._
  // Fields are specified by the creator function "field(initial value)".
  // Additional stuff like restrictions can just be put after it. The 
  // field automatically gets the name of the field variable. Thus the
  // field variable should be unique (field name is only used for output
  // generation and not for any calculations).
  /** Amount of protein */
  var p = field(_p) >= 0.0
  /** DNA */
  var c = field(_c) >= 0.0
  /** Ribosomes */
  var r = field(_r) >= 0.0
  /** Resource (energy, ATP) */
  var a = field(_a) >= 0.0

  /** Real ribosomes (as above is measured in number of proteins) */
  var r_real = field(0.0)
  /** Some scaling of a to fit plots nicer (remember, a is arbitrary) */
  var a_real = field(0.0) // scaled version of a (as a is arbitrary anyways)

  /** External resource */
  var ew = field(50.0) >= 0.0
  /** "Better" resource (is transformed from external resource by cell) */
  var bw = field(0.0) >= 0.0
  /** Resource-disposal protein */
  var p_w = field(0.0) >= 0.0
  /** Transcription factor that measures A */
  var tf_a = field(0.0) >= 0.0
  /** Transcription factor that measures external resource */
  var tf_ew = field(0.0) >= 0.0
}

/**
 * The Resource Disposal system implements the simple mathematical model
 * used by Bollenbach et al. to simulate cell behavior. In addition
 * there are components that handle "cleaning" of resource into some
 * sort of useful resource. 
 */
class ResourceProcessingModel(iSr: Double) extends StormModel {
  // State type has to be set to the state associated with this model
  // This allows for static typechecking of all field accesses
  type StateType = ResourceProcessingState

  // Import some stuff to make access easier (instead of "Constants.A"
  // just write "A")
  import Constants._
  import Functions._

  // Initialize the state vector
  lazy val stateVector = ResourceProcessingState()
  // Initialize the processes working on the state. This is of the form
  // "() => new Process" as this specifies a creator function that is able
  // to instantiate new processes on the fly. This is because processes
  // run many times in a distributed manner
  ++(() => new Metabolism)
  ++(() => new ResourceDispProc)
  ++(() => new ResourceDispProtTranslation)
  /*lazy val processes: Array[() => StormProcess[ResourceProcessingState]] = Array(
    // It is a little inconvenient having to write () => ... {override val id = ...}
    // Maybe write a macro that allows to just write new Metabolism
    // Problems might be if the modeler wants to use abstract types for his processes
    () => new Metabolism,
    () => new ResourceDispProc,
    () => new ResourceDispProtTranslation)*/

  // Some meta-data
  val title = "Resource Processing Model Based on the Bollenbach Model"
  val desc = "An extended version of the model used in the paper 'Nonoptimal Microbial Response to Antibiotics Underlies Suppressive Drug Interactions."
  val authors = "Tobias Bollenbach, Hristiana Pashkuleva, Dominik Bucher, Vincent Danos"
  val contributors = "Hristiana Pashkuleva, Dominik Bucher, Vincent Danos"

  // Specify ovservables that are plotted right after the simulation ends
  override val observables = {
    import stateVector._
    List(a_real, ew, bw, p_w)
  }

  // Use this function to update state (internal dependencies). The function
  // will be called whenever there is a change in state. 
  def calcDependencies(state: ResourceProcessingState) = {
    import state._

    tf_a() = K_TFa * a()
    tf_ew() = K_TFew * ew()

    r_real() = r() / pr
    a_real() = a() * 40.0
  }

  // Definition of a process, in this case metabolism, which is responsible
  // for metabolic functions of the cell
  class Metabolism extends StormProcess[ResourceProcessingState] {
    def name = "Metabolism"
    // The evolve method is the most important one, as it specifies what the
    // process is doing with the state
    def evolve(state: ResourceProcessingState, t: Double, dt: Double) = {
      // Import the state, again for shorter access of state variables
      import state._

      // Do any calculations and state changes you wish
      val sr = Nrrn * No *  sr0 * f_fres(a(), V) //iSr // Nrrn * No * math.min(sr0 /*opt??*/ , sr0 * f_fres(a(), f_V(r(), p())))
      val sp = (1.0 - eta) * kp0 * f_fres(a(), V) * rho * r() // f_spec_sp(a(), r(), p(), sr)
      val sc = Nf * 1.0 / (2.0 * f_tauC(a(), r(), p()))
      val sa = _va

      p() += (sp - _g * p()) * dt
      c() += (sc - _g * c()) * dt
      r() += (sr - _g * r()) * dt
      a() += (sa - (_g + kdeg) * a() - (epsP * sp + epsR * sr + epsC * sc)) * dt
    }
  }

  class ResourceDispProc extends StormProcess[ResourceProcessingState] {
    def name = "ResourceProcessing"
    def evolve(state: ResourceProcessingState, t: Double, dt: Double) = {
      import state._

      val v_ew = v_maxew * (p_w() * ew()) / (K_ew + ew())
      ew() -= v_ew * dt
      bw() += v_ew * dt
    }
  }

  class ResourceDispProtTranslation extends StormProcess[ResourceProcessingState] {
    def name = "ResourceProcessing Protein Translation"
    def evolve(state: ResourceProcessingState, t: Double, dt: Double) = {
      import state._

      val c_wdp = u0 + u1 * (tf_ew() + tf_a()) / (K_wdp + tf_ew() + tf_a())
      val v_wdp = c_wdp * rho_maxtr / (3 * n_wdp) * a() / (theta_wdp + a())
      //println(s"c_wdp: $c_wdp, v_wdp: $v_wdp")

      p_w() += (v_wdp - _g * p_w()) * dt
      a() -= v_wdp / 160.0
    }
  }
}

// An object specifying some constants
object Constants {
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

  // Dependent properties ------------------------------------------------------------
  // Initial numbers
  val _g = 0.69 // h^-1; Cell division rate, growth rate
  val _r = 1.0 //1.35 * 10000.0 // # ribosomes per cell
  val _p = 40.0 //2.4 * 1000000.0 // # proteins per cell
  val _c = 1.8 // Genome equivalents of DNA per cell
  val _a = 1.0 // # of resources per cell (called atp here); arbitrarily chosen
  
  val kp = 0.038 // s^-1; Rate of protein synthesis per ribosome
  val tauC = Functions.f_tauC(_a, _r, _p) // 50.0 // min; Replication time of chromosome
  val tauD = Functions.f_tauD(_a, _r, _p) // 25.0 // min; Delay before cell division
  val Nf = Functions.f_Nf(_a, _r, _p, _g) // 2.1 // # of replication forks
  val No = Functions.f_No(_a, _r, _p, _g) // 2.4 // # of replication origins
  val V = Functions.f_V(_r, _p) // 1.0 // um^3; Cell volume
  val _va = 2.42 // h^-1; Resource influx
  // val _sr = Nrrn *No *  sr0 * Functions.f_fres(_a, V)
  // val _sp = po * _g * No
  val eta = Functions.f_eta(_r, _p) //pr * _sr / (_sp + pr * _sr) // 0.11 // Ribosomal protein fraction (0 < eta < 1)
  println(s"tauC: $tauC, tauD: $tauD, Nf: $Nf, No: $No, V: $V, eta: $eta")

  val K_TFa = 0.1 // 0.0001
  val K_TFew = 0.1 // 0.0001
  val u0 = 0.001 * 3600 / 100
  val u1 = 0.1 * 3600 / 100
  val K_wdp = 0.01 * 3600
  val rho_maxtr = 85.0 * 3600
  val n_wdp = 300.0
  val theta_wdp = 1260.0 / 50.0
  val v_maxew = 12.1
  val K_ew = 100.0
}

// An object specifying some functions
object Functions {
  import Constants._

  def f_V(r: Double, p: Double) = kV * (p + pr * r)
  def f_eta(r: Double, p: Double) = pr * r / (p + pr * r)
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
   * Some stupid numerical approximation of functions. Used by the bollenbach model. Not sure 
   * if correct approach tough. 
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