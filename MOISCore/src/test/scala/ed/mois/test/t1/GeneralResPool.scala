/*
 * Contains a general resource pool skeleton.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.test.t1

import ed.mois.core.sim.Simulator
import ed.mois.core.util.Verbosity
import ed.mois.core.sim.state.StateSkeleton
import ed.mois.core.sim.state.State

trait GeneralResPoolSkeleton extends StateSkeleton {
  val Res = prop(500.0, "Res")
}

class GeneralResPoolSketch(val name: String) extends GeneralResPoolSkeleton

class GeneralResPool(name: String, simulator: Simulator) extends State(name, simulator) with GeneralResPoolSkeleton {
  val verbosity = Verbosity.emergency
  val seed = 0
}
