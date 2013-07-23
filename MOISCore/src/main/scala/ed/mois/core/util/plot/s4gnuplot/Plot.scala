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

import scala.collection.immutable.Range

case class Plot(data:Seq[Seq[Double]] = Seq(),
                title:String = "",
                lines:Seq[Line] = Seq(),
                xlabel:String = "",
                ylabel:String = "",
                grid:Boolean = true,
                offsets:String = "graph 0.1, graph 0.1, graph 0.1, graph 0.1",
                xrange: String = "",
                yrange: String = "",
                custom:Seq[String] = Seq()
                )
{ plot =>
  def data(dat:Seq[Seq[Double]]):Plot = plot.copy(data = dat)
  def dataInt(dat:Seq[Seq[Int]]):Plot = plot.copy(data = dat.map(_.map(_.toDouble)))
  def dataIntLine(dat:Seq[Int]):Plot = plot.copy(data = dat.map(d => Seq(d.toDouble)), lines = Seq((new Line).With("lines")))
  def dataLine(dat:Seq[Double]):Plot = plot.copy(data = dat.map(d => Seq(d)), lines = Seq((new Line).With("lines")))
  def title(s:String):Plot = plot.copy(title = s)
  def xlabel(s:String):Plot = plot.copy(xlabel = s)
  def ylabel(s:String):Plot = plot.copy(ylabel = s)
  def gridOn:Plot = plot.copy(grid = true)
  def gridOff:Plot = plot.copy(grid = false)
  def offsets(s:String):Plot = plot.copy(offsets = s)
  def xrange(s:String):Plot = plot.copy(xrange = s)
  import collection.immutable.Range
  def xrange(r:Range):Plot = plot.copy(xrange = "[%d:%d]" format (r.head, r.last))
  def yrange(s:String):Plot = plot.copy(yrange = s)
  def yrange(r:Range):Plot = plot.copy(yrange = "[%d:%d]" format (r.head, r.last))
  def line(fn: Line => Line):Plot = plot.copy(lines = lines :+ fn(new Line))
  def custom(s:Seq[String]):Plot = plot.copy(custom = s)
  /** Simply adds provided line to the end of the script, right before "plot" call */
  def custom(s:String):Plot = plot.copy(custom = custom :+ s)
  def plot {
    Gnuplot.plot(plot)
  } 
}
