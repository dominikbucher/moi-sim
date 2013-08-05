/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm

import scala.collection.immutable.TreeMap
import scala.util.control.Breaks._

import ed.mois.core.storm._

/**
 * A change describes the change on a system in a given time interval. 
 * The origin is the id of the process that requested the change. 
 */
case class StormChange(origin: Int, t: Double, dt: Double, chg: Map[Int, Any]) {
	val tEnd = t + dt
}

/**
 * Helper class to handle changes, like intersections, merges, slices. 
 */
trait ChangeHelper {
	/**
	 * Intersects a list of changes, meaning that changes are linearly applied to the initial
	 * state by slicing them and integrating them until violations are detected. If any 
	 * violations happen, the violating time and violating changes are returned so
	 * the simulator can decide what to do with them. 
	 */
	def intersect(state: StormState[_ <: StormState[_]], init: collection.mutable.Map[Int, StormField[_]], chgs: List[StormChange], t: Double, dt: Double): 
		Option[Tuple3[Double, Double, List[StormChange]]] = {

		// Slice up the time frame into interesting parts
		val sliced = slices(chgs)
		// Create an empty var to store violators in any time slice
		var violators: Option[List[StormChange]] = None
		// Rush through slices and merge all the changes
		for (s <- sliced) {
			// Only consider slices within time window
			if (s._1._1 >= t && s._1._2 <= t + dt) {
				// Reset dirty fields
				init.foreach {i => i._2.dirty = false}
				// Store state to be able to revert in case of violations
				val stateCpy = state.dupl
				// Slices have the form ((t_start, t_end), changes)
				violators = tryMerge(init, s._2, s._1._1, s._1._2)
				// If there is an error in time slice s, exit intersect function and return
				// a tuple containing (errorSliceTStart, errorSliceTEnd, InvolvedChanges)
				if (violators.isDefined) {
					// Reset state
					state.fields.foreach(f => state.fields(f._1) = stateCpy.fields(f._1))
					return Some(s._1._1, s._1._2, violators.get)
				}
			}
		}
		// If the whole intersection went ok, return None, indicating there are no errors
		return None
	}

	/**
	 * Tries to merge a set of changes into the state denoted as init. Returns a list of violators
	 * if the merge fails. 
	 */
	def tryMerge(init: collection.mutable.Map[Int, StormField[_]], chgs: List[StormChange], t: Double, tEnd: Double): Option[List[StormChange]] = {
		// For each field there is a number of involved changes, denoted by this map (id) -> InvolvedChanges
    	val involved = collection.mutable.Map.empty[Int, List[StormChange]]
    	// All the ids of fields that were violated in this merge
    	var violatedFields = List.empty[Int]
    	// For every change and every field, merge this into the init vector
    	for { chg <- chgs
    		fieldChg <- chg.chg } {
    			//val s = s"Merging ${fieldChg._1}: ${fieldChg._2} into ${init(fieldChg._1)} ($t, ${chg.dt}, ${tEnd - t})"
    			// The merge function returns false if the merge failed -> add the id to violated fields in that case
    			if (!init(fieldChg._1).merge(fieldChg._2, chg.dt, tEnd - t)) violatedFields = fieldChg._1 :: violatedFields
    			//println(s + s", yields ${init(fieldChg._1)}")
    			// Add the change to the involved list
				addToListMap(involved, fieldChg._1, chg)
    		}

    	// If there were any violations, gather up all the involved changes and return them
    	if (violatedFields.length > 0) Some(violatedFields.map(vf => involved(vf)).flatten.distinct)
    	else None
	}

	/** 
	 * Transforms a list of changes to a map which contains "interesting" time
	 * slices (tStart, tEnd) as keys and all the changes that are involved in an interesting 
	 * slice at that given time UNTIL the next time point. 
	 */
	def slices(chgs: List[StormChange]): TreeMap[Tuple2[Double, Double], List[StormChange]] = {
		// Create the return value map (consists of (tStart, tEnd) -> InterestingChanges)
    	val m = collection.mutable.Map.empty[Tuple2[Double, Double], List[StormChange]]

    	// Collect all interesting time points (mainly start and end times of changes)
		val timePoints = chgs.map(chg => List(chg.t, chg.tEnd)).flatten.distinct.sorted.sliding(2).toList
		chgs.foreach { chg => 
			// Get all time points that are interesting for this change
			val tps = timePoints.filter(tp => chg.t <= tp(0) && tp(0) < chg.tEnd)
			// And add the change to all tuples of interesting time points
			tps.foreach(tp => addToListMap(m, (tp(0), tp(1)), chg))
		}    	
		//println(s"Slicing: $chgs, timePoints: $timePoints")


		TreeMap(m.toArray:_*)
	}

	/**
	 * Adds an element to any map with a list as value (used because lists need to be
	 * created in the first addition).
	 */
	def addToListMap[A, B](m: collection.mutable.Map[A, List[B]], a: A, b: B) {
		if (m.contains(a)) {
			m(a) = b :: m(a)
		} else {
			m(a) = List(b)
		}
	}
}