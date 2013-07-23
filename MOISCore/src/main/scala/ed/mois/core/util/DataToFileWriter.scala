/*
 * Contains a simple writer that writes streams to a file.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

import scalax.io._

class DataToFileWriter {
  val clearOutput = Resource.fromOutputStream(new java.io.FileOutputStream("data.dat"))
  clearOutput.write("")
  val output = Resource.fromFile("data.dat")
  
  def writeDataHeader(dh: Array[String]) {
    output.write("t " + dh.mkString(" ") + "\n")
  }
  def writeDataPoints(t0: Double, dps: Array[Any]) {
    output.write(t0 + " " + dps.mkString(" ") + "\n")
  }
}