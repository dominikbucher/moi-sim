/*
 * Contains some helper function oriented towards arrays / matrices.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

trait Helpers {
  /**
   * Adds elements in a double array (sum). Implemented with a while loop
   * to prevent casting to higher-order objects. This sould be one
   * of the fastest implementations for the jvm.
   *
   * @param ad The array containing the values to be added.
   * @return The sum of all elements in the array.
   */
  def adSum(ad: Array[Double]): Double = {
    var sum = 0.0
    var i = 0
    while (i < ad.length) { sum += ad(i); i += 1 }
    sum
  }

  /**
   * Sums all elements in second dimension of a 2D array.
   *
   * @param ad The 2D array where elements should be summed.
   * @return A new array containing the sums of the rows.
   */
  def ad2DSum(ad: Array[Array[Double]]) = {
    var sumAr = Array.ofDim[Double](ad.length)
    var i = 0
    var j = 0
    while (i < ad.length) {
      while (j < ad(i).length) {
        sumAr(i) = sumAr(i) + ad(i)(j)
        j += 1
      }
      j = 0
      i += 1
    }
    sumAr
  }

  /**
   * Searches an array for any element that is not zero (0.0).
   *
   * @param ad The array to search.
   * @return True if it contains at least one element != 0.0.
   */
  def adAny(ad: Array[Double]): Boolean = {
    var found = false
    var i = 0
    while (i < ad.length) {
      if (ad(i) != 0.0) {
        found = true; i = ad.length;
      }
      i += 1
    }
    found
  }

  /**
   * Creates continuous indices after indices in an input array.
   *
   * @param numElements The number of indices to create.
   * @param ad After the last element of this array.
   * @return An array of indices.
   */
  def indicesAfter(numElements: Int, ai: Array[Int]): Array[Int] = {
    (1 to numElements).map(_ + ai(ai.length - 1)).toArray
  }

  /**
   * Modifies an array with the given function. The function is applied on the array elements.
   *
   * @param ad The array to modify.
   * @param f The function to modify array elements.
   * @return The modified array.
   */
  def modifyArray(ad: Array[Double], f: Double => Double): Array[Double] = {
    var i = 0
    while (i < ad.length) {
      ad(i) = f(ad(i))
      i += 1
    }
    ad
  }

  /**
   * Modifies an array with the given function. The function is applied on the array elements
   * only at specified indices.
   *
   * @param ad The array to modify.
   * @param indices The indices which to modify.
   * @param f The function to modify array elements.
   * @return The modified array.
   */
  def modifyArray(ad: Array[Double], indices: Array[Int], f: Double => Double): Array[Double] = {
    var i = 0
    while (i < indices.length) {
      ad(indices(i)) = f(ad(indices(i)))
      i += 1
    }
    ad
  }

  def mergeArrays(ad: Array[Double], inputArray: Array[Double],
    indices: Array[Int], f: (Double, Double) => Double): Array[Double] = {
    var i = 0
    while (i < indices.length) {
      ad(indices(i)) = f(ad(indices(i)), inputArray(indices(i)))
      i += 1
    }
    ad
  }

  /**
   * @param ad
   * @param inputArray
   * @param f
   * @return
   */
  def mergeArrays(ad: Array[Double], inputArray: Array[Double], f: (Double, Double) => Double) = {
    if (ad.length == inputArray.length) {
      var i = 0
      while (i < ad.length) {
        ad(i) = f(ad(i), inputArray(i))
        i += 1
      }
      ad
    } else {
      throw new Exception("Arrays " + ad + " and " + inputArray + " don't have the same length.")
    }
  }

  /**
   * @param ad
   * @param inputArray
   * @return
   */
  def copyArray(ad: Array[Double], inputArray: Array[Double]): Array[Double] = {
    if (ad.length == inputArray.length) {
      var i = 0
      while (i < ad.length) {
        ad(i) = inputArray(i)
        i += 1
      }
      ad
    } else {
      throw new Exception("Arrays " + ad + " and " + inputArray + " don't have the same length.")
    }
  }
}