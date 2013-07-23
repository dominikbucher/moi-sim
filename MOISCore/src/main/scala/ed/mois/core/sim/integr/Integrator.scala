/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.integr

import ed.mois.core.sim.Simulator
import ed.mois.core.comm.ChangeRequest

abstract class Integrator(sim: Simulator) {

  def integrate(chgReqs: List[ChangeRequest]): Double
  
}