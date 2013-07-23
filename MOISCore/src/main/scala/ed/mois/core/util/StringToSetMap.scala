/*
 * Contains functions to convert strings to sets.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

/**
 * References things mapped by state names. 
 *
 * @param <T> Type of things to be referenced. 
 */
case class StringToSetMap[T] {
  val data = collection.mutable.Map.empty[String, collection.mutable.Set[T]]
  
  def apply(key: String) = {
    data(key)
  }

  def add(key: String, dataPoint: T) = {
    if (data.contains(key)) {
      data(key) += dataPoint
    } else {
      data += (key -> collection.mutable.Set(dataPoint))
    }
    this
  }

  def add(key: String) = {
    data += (key -> collection.mutable.Set())
    this
  }

  def remove(key: String, dataPoint: T) = {
    if (data.contains(key)) {
      data(key) -= dataPoint
    }
    this
  }

  def remove(key: String) = {
    data.remove(key)
    this
  }
  
  override def toString = {
    data.toString
  }
}
