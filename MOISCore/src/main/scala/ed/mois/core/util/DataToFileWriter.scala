/*
 * Contains a simple writer that writes streams to a file.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

import scalax.io._
import ed.mois.core.storm._
import scala.collection.immutable.TreeMap

class DataToFileWriter {
  val fileName = "data.dat"

  val output: Output = Resource.fromFile("data.dat")
  
  def writeDataHeader(dh: Array[String]) {
    val clearOutput = Resource.fromOutputStream(new java.io.FileOutputStream(fileName))
    clearOutput.write("")
    output.write("t " + dh.mkString(" ") + "\n")
  }
  def writeDataPoints(t0: Double, dps: Array[Any]) {
    output.write(t0 + " " + dps.mkString(" ") + "\n")
  }
  def writeStormDataPoints(p: Tuple2[Double, StormState[_]]) {
  	output.write(p._1 + " " + p._2.fields.map(_._2).mkString(" ") + "\n")
  }

  def writeStormData(fileN: String, model: StormModel, d: TreeMap[Double, StormState[_]]) {
    for {
      outProcessor <- Resource.fromOutputStream(new java.io.FileOutputStream(fileN)).outputProcessor
      out = outProcessor.asOutput
    } {
      // Write data header
      out.write("t " + d.head._2.fields.map(f => model.stateVector.fieldNames(f._1)).toArray.mkString(" ") + "\n")
      // Write data
      d.foreach{ p =>
        out.write(p._1 + " " + p._2.fields.map(_._2).mkString(" ") + "\n")
      }
    }
  }
}