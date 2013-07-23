/**
 * This is copied from https://github.com/dominikbucher/s4gnuplot. Please use the github link
 * to see about forking and original version. 
 * 
 * Copyright (C) 2012 Platon Pronko, modified by Dominik Bucher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
package ed.mois.core.util.plot.s4gnuplot

import Util._
import scala.sys.process.stringSeqToProcess

object Gnuplot {
	val os = System.getProperty("os.name")
	
  /** creates file in /tmp/, with provided lines in it */
  def create_file(lines: Seq[String]):String = {
    val fileName = "gnuplot_%d.gp" format (math.random * 100000000 toInt)
    lines ##> new java.io.File(fileName)
    fileName
  }
  
  /** executes the script using gnuplot */
  def execute_script(fileName:String) = {
	import sys.process._
	if (os.toLowerCase.contains("windows")) {
		val p = Runtime.getRuntime().exec("""cmd /c start "gnuplot" /separate "gnuplot" -p """ + fileName)
		p.waitFor();
	} else {
		Seq("gnuplot", "-persist", fileName) !
	}
  }

  /** cleans up the files in /tmp */
  def clean_up {
    import sys.process._
	for {
		files <- Option(new java.io.File(".").listFiles)
		file <- files if file.getName.startsWith("gnuplot_")
	} file.delete()
  }

  /** Execute gnuplot on provided plot object. */
  def plot (plot:Plot) {
    import plot._
	clean_up
    val dataFile = create_file(data.map(_.mkString("  ")))
    val scriptFile = create_file(
      Seq(
        "set offsets %s" format offsets,
        "set title '%s'" format title,
        "set xlabel '%s'" format xlabel,
        "set ylabel '%s'" format ylabel,
        (if (grid) "set grid" else "unset grid"),
        xrange iff ("set xr %s" format xrange),
        yrange iff ("set yr %s" format yrange)
      ) ++ custom ++
      Seq(("plot " + lines.map("'%1$s' " + _.toString).mkString(", ")) format dataFile) ++ Seq("pause -1", "close")
    )
    execute_script(scriptFile)
  }

  /** Creates a default plot builder */
  def newPlot = new Plot 
  
}
