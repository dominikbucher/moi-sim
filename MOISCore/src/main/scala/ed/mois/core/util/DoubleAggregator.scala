/*
 * Contains an aggregator that can aggregate double values from actors.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

/**
 * The double aggregator aggregates doubles up to a certain amount and
 * makes the total as well as the single values available (stored in a
 * map with a string identifier). Call reset to reset the aggregator to
 * zero.
 */
class DoubleAggregator(var size: Int) {
  var totalNr = 0
  var total = 0.0
  var single = scala.collection.mutable.Map.empty[String, Double]

  /**
   * Resets the aggregator to zero, so that aggregation process can restart.
   */
  def reset = {
    totalNr = 0
    total = 0.0
    single.clear
  }

  /**
   * Aggregates values by storing a single value with a given id. Makes
   * a sum of the values available, and returns true if the size limit
   * is reached.
   *
   * @param id
   * @param value
   * @return
   */
  def aggregate(id: String, value: Double): Boolean = {
    total += value
    single(id) = value
    totalNr += 1
    complete_?
  }

  /**
   * Queries the aggregator for completeness of aggregation.
   *
   * @return
   */
  def complete_?(): Boolean = {
    if (totalNr >= size) true
    else false
  }

  /**
   * Gets a value with a given id from the aggregator.
   *
   * @param id
   * @return
   */
  def apply(id: String): Double = {
    single(id)
  }
}