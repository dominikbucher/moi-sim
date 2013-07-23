/*
 * Contains different simulator verbosity levels
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

/**
 * How verbose the output of something should be.
 */
sealed class Verbosity extends Enumeration {
  type Verbosity = Value
  val emergency = Value(1, "emergency")
  val error = Value(2, "error")
  val warning = Value(3, "warning")
  val information = Value(4, "information")
  val debug = Value(5, "debug")
}

object Verbosity extends Verbosity