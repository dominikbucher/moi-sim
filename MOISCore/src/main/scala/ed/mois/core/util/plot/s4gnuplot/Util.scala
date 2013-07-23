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

import java.io._

object Util {
  class RicherFile(f: File) {
    def printHere(append:Boolean = true)(op: java.io.PrintWriter => Unit) {
      if (!f.exists) {
        f.createNewFile
      }
      val p = new java.io.PrintWriter(new OutputStreamWriter(new FileOutputStream(f, append), "UTF-8"))
      try { op(p) } finally { p.close() }
    }
    /** So the caller can omit the parenthesis after "printHere" */
    def printHere(op: java.io.PrintWriter => Unit) {
      printHere(append = false)(op)
    }
  }
  implicit def wrapperFile(f: File):RicherFile = new RicherFile(f)


  class PimpedStringSeq(strings:Seq[String]) {
    def ##>(f:File) {
      f.printHere(append = false) { p =>
        strings.foreach(s => p.println(s))
      }
    }
    def ##>>(f:File) {
      f.printHere(append = true) { p =>
        strings.foreach(s => p.println(s))
      }
    }
    def ##>(s:String) {
      ##>(new File(s)) 
    }
    def ##>>(s:String) {
      ##>>(new File(s))
    }
  }
  implicit def toPimpedStringSeq(strings:Seq[String]) = new PimpedStringSeq(strings)

  class PimpedString(s:String) {
    // returns result of the function is s is empty
    def or(fn: => String) = if (s.trim.size > 0) s else fn
    // returns result of the function if s is non-empty
    def iff(fn: => String) = if (s.trim.size > 0) fn else s
  }
  implicit def toPimpedString(s:String) = new PimpedString(s)

}
