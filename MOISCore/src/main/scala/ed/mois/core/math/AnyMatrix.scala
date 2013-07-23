/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.math

import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer

/**
 * @author Dominik Bucher
 *
 * Matrix that stores any values and allows to remove a set of indexed Srows.
 */
class AnyMatrix(val nRows: Int, val nCols: Int) {
  var fields: Array[Array[Any]] = Array.fill(nRows)(Array.fill(nCols)(None))

  def apply(r: Int, c: Int) = {
    fields(r)(c)
  }

  def apply(r: Int) = {
    fields(r)
  }

  def update(r: Int, c: Int, v: Any) {
    fields(r)(c) = v
  }

  def removeCols(idxs: List[Int]) = {
    fields.map(f => f.zipWithIndex.filter {
      ff => !idxs.contains(ff._2)
    }.map { _._1 })
  }

  def removeRows(idxs: List[Int]) = {
    fields.zipWithIndex.filter {
      ff => !idxs.contains(ff._2)
    }.map { _._1 }
  }

  def to2DArray[T: ClassTag]: Array[Array[T]] = {
    fields.map(_.map(_.asInstanceOf[T]))
  }

  override def toString = {
    fields.map(f => f.mkString("[ ", ", ", " ]")).mkString("[ ", ", ", " ]")
  }
}