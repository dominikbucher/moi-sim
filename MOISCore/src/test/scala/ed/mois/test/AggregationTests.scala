/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */
package ed.mois.test

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.immutable.TreeMap
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/**
 * Tests some aggregation stuff (speed, needed for some arguments in the thesis).
 */
object AggregationTests extends App {
  var a = Array.ofDim[Double](28, 2500)
  val startTime = System.currentTimeMillis

  (0 to 10000).foreach { i =>
  	var n = 0
  	var p = 0
  	while(n < 2500) {
	  // (0 to 2499).foreach { n =>
	  	var res = 0.0
	  	// (0 to 27).foreach { p => 
	  	while (p < 28) {
	  		res += a(p)(n)
	  		p += 1
	  	}
	  	p = 0
	  	n += 1
	  	res
	  }
	}

  //println(System.currentTimeMillis - startTime)
  println((System.currentTimeMillis - startTime).toDouble / 10000.0)
}