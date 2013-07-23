/*
 * Contains functions to import data from files.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

import scala.io.Source
/**
 * Allows fast import of double arrays from a file (as in exported from MATLAB).
 */
object FileImport {
  def double2DArrayFromFile(fileName: String): Array[Array[Double]] = {
    val src = Source.fromURL(getClass.getResource(fileName))

    val floatingPointRegEx = """[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?""".r
    val array = src.getLines.map(l => {
      floatingPointRegEx.findAllMatchIn(l).map(m => m.matched.toDouble).toArray
    }).toArray

    //println(array.map(_.map(e => e.toString).mkString(", ")).mkString("], ["))
    array
  }
}