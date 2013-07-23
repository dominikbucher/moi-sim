/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.math

/**
 * Defines mathematical functions not found in java / scala math.
 */
trait CMMath {
  /**
   * Get the greatest common divisor of two integers.
   *
   * @param x Int x.
   * @param y Int y.
   * @return The greatest common divisor of x and y.
   */
  def gcd(x: Int, y: Int): Int =
    if (y == 0) x
    else gcd(y, x % y)

  /**
   * Get the greatest common divisor of an array of integers.
   *
   * @param ad The array of integers.
   * @return The greatest common divisor of them.
   */
  def gcd(ad: Array[Int]): Int = {
    var result: Int = ad(0)
    for (i <- 1 until ad.length) {
      result = gcd(result, ad(i))
    }
    return result
  }
}