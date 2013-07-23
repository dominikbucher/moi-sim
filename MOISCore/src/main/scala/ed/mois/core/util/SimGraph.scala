/*
 * Contains a simulation graph outline class.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

import ed.mois.core.sim.prop.PropertyId

case class SimGraph(states: List[StateEntry], processes: List[ProcessEntry]) {
  override def toString = {
    "Simulation Graph: \n  States:\n    " + states.mkString("\n    ") + "\n  Processes:\n    " + processes.mkString("\n    ")
  }
}
case class StateEntry(name: String, props: Set[PropertyId])
case class ProcessEntry(name: String, writeProps: Set[PropertyId],
  readProps: Set[PropertyId])