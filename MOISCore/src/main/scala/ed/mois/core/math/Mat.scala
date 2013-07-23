/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.math

import java.util._
import org.ejml.ops._
import org.ejml.simple.SimpleMatrix
import org.ejml.data.DenseMatrix64F
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt
import org.ejml.alg.dense.decomposition.chol._
import org.ejml.alg.dense.decomposition.qr._
import org.ejml.alg.dense.linsol.qr._
import scala._
import scala.collection.generic._

/**
 * Creates a matrix backed by the EJML matrix library.
 *
 * @param nRows Number of rows of matrix.
 * @param nCols Number of columns of matrix.
 * @param initV The initial value of the matrix.
 */
class Mat(val sm: SimpleMatrix) {
  def this(nRows: Int, nCols: Int) = {
    this(new SimpleMatrix(nRows, nCols))
  }

  def this(ad: Array[Array[Double]]) = {
    this(new SimpleMatrix(ad))
  }
  def this(ad: Array[Double]) = {
    this(ad.size, 1)
    var k = 0
    while (k < ad.size) {
      this(k, 0) = ad(k)
      k += 1
    }
  }
  def this(nRows: Int, nCols: Int, initV: Double) = {
    this(new SimpleMatrix(nRows, nCols))
    var n = 0
    while (n < nRows) {
      var m = 0
      while (m < nCols) {
        this(n, m) = initV
        m += 1
      }
      n += 1
    }
  }
  /**
   * Dimension is always two.
   */
  def dim = 2

  /**
   * Number of rows in the matrix.
   *
   * @return
   */
  def nRows = sm.numRows
  /**
   * Number of columns in the matrix.
   *
   * @return
   */
  def nCols = sm.numCols
  /**
   * The total number of elements in the matrix.
   *
   * @return
   */
  def length = nRows * nCols
  /**
   * The size as a tuple (N x M).
   *
   * @return
   */
  def size = (nRows, nCols)

  /**
   * Whether the matrix is a vector.
   *
   * @return
   */
  def isVector = sm.isVector

  def apply(ind: Int) = {
    sm.get(ind % nCols, ind / nRows)
  }

  def apply(o: RowColMatSelector) = {
    // TODO think about using sm.extractVector
    val ret = new Mat(nRows * nCols, 1)
    for (i <- 0 until nRows) {
      for (j <- 0 until nCols) {
        ret.sm.set(i + j * nRows, 0, sm.get(i, j))
      }
    }
    ret
  }

  def apply(row: Int, col: Int) = {
    sm.get(row, col)
  }

  def apply(row: RowColMatSelector, col: Int) = {
    val ret = new Mat(nRows, 1)
    for (i <- 0 until nRows) {
      ret.sm.set(i, 0, sm.get(i, col))
    }
    ret
  }

  def apply(row: Int, col: RowColMatSelector) = {
    val ret = new Mat(1, nCols)
    for (i <- 0 until nCols) {
      ret.sm.set(0, i, sm.get(row, i))
    }
    ret
  }

  // TODO Check syntax!
  def apply(ind: Array[Int], col: Int): Mat = {
    val ret = new Mat(ind.length, 1)
    for (i <- 0 until ind.length) {
      ret(i, 0) = this(ind(i), col)
    }
    ret
  }

  def apply(ind: Array[Int]): Mat = {
    val ret = new Mat(ind.length, nCols)
    for (i <- 0 until ind.length) {
      for (j <- 0 until nCols) {
        ret(i, j) = this(ind(i), j)
      }
    }
    ret
  }

  def update(ind: Int, newVal: Double) = {
    sm.set(ind % nCols, ind / nRows, newVal)
  }

  def update(row: Int, col: Int, newVal: Double) = {
    sm.set(row, col, newVal)
  }

  def update(row: RowColMatSelector, col: Int, newVal: Double) = {
    for (i <- 0 until nRows) {
      sm.set(i, col, newVal)
    }
  }

  def update(row: Int, col: RowColMatSelector, newVal: Double) = {
    for (i <- 0 until nCols) {
      sm.set(row, i, newVal)
    }
  }

  /* (non-Javadoc)
 * @see java.lang.Object#clone()
 */
  override def clone = {
    val smc = sm.copy
    new Mat(smc)
  }

  /**
   * Alias for clone.
   *
   * @return
   */
  def copy = {
    clone
  }

  /* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
  override def toString = {
    s"Matrix of dimension $dim: size $nRows x $nCols"
  }

  def flatMap(f: Double => Double): Array[Double] = {
    val ret = Array.ofDim[Double](length)
    var i = 0
    var j = 0
    while (i < nRows) {
      while (j < nCols) {
        ret(i * nRows + j) = f(sm.get(i, j))
        j += 1
      }
      j = 0
      i += 1
    }
    ret
  }

  def toIntArray = {
    val ret = Array.ofDim[Int](length)
    var i = 0
    var j = 0
    while (i < nRows) {
      while (j < nCols) {
        ret(i * nCols + j) = sm.get(i, j).toInt
        j += 1
      }
      j = 0
      i += 1
    }
    ret
  }

  /**
   * Gets the backing double array.
   *
   * @return
   */
  def data = sm.getMatrix.getData

  /**
   * Calculates the dot product between two matrices.
   *
   * @param that The other matrix.
   * @return The dot product.
   */
  def dot(that: Mat) = {
    sm.dot(that.sm)
  }

  /**
   * Returns a reference to the matrix that it uses internally. This is useful
   * when an operation is needed that is not provided by this class.
   *
   * @return Reference to the internal DenseMatrix64F.
   */
  def getMatrix = {
    sm.getMatrix
  }

  /**
   * Cross product (point-wise) of a matrix with another matrix.
   *
   * @param that The other matrix.
   * @return The cross product.
   */
  def cross(that: Mat): Mat = {
    var nv = new Mat(this.nRows, this.nCols)
    var i = 0; var j = 0;
    while (i < nRows) {
      j = 0
      while (j < nCols) {
        nv(i, j) = this(i, j) * that(i, j)
        j += 1
      }
      i += 1
    }
    return nv
  }

  /**
   * Short form for cross product.
   *
   * @param that
   * @return
   */
  def x(that: Mat): Mat = {
    cross(that)
  }

  /**
   * Performs a matrix multiplication operation.
   *
   * C = A * B
   *
   * Where C is the returned matrix, A is this matrix and B is the passed in matrix.
   *
   * A and B are not modified.
   *
   * @param that The matrix B.
   * @return The matrix C.
   */
  def *(that: Mat): Mat = {
    var rN = this.nRows; var rM = this.nCols;
    var sN = that.nRows; var sM = that.nCols;

    var ret = new SimpleMatrix(sm.getMatrix.numRows, that.sm.getMatrix.numCols)
    CommonOps.mult(sm.getMatrix, that.sm.getMatrix, ret.getMatrix)

    new Mat(ret)
  }

  /**
   * Performs element-wise multiplication. The target matrix needs to have the same
   * dimensions as this matrix.
   *
   * @param that The matrix to multiply with.
   * @return A new matrix containing the multiplied values.
   */
  def @*(that: Mat): Mat = {
    var ret = new SimpleMatrix(sm.getMatrix.numRows, sm.getMatrix.numCols)
    CommonOps.elementMult(sm.getMatrix, that.sm.getMatrix, ret.getMatrix)
    new Mat(ret)
  }

  /**
   * Multiplies all elements of a matrix by a value.
   *
   * @param that The value to multiply with.
   * @return A new matrix containing the multiplied values.
   */
  def @*(that: Double): Mat = {
    new Mat(this.sm.scale(that))
  }

  /**
   * Divides all elements of a matrix by a value.
   *
   * @param that The value to divide by.
   * @return A new matrix containing the divided values.
   */
  def @/(that: Double): Mat = {
    var ret = new SimpleMatrix(sm.getMatrix.numRows, sm.getMatrix.numCols)
    CommonOps.divide(that, sm.getMatrix, ret.getMatrix)
    new Mat(ret)
  }

  /**
   * Performs element-wise division. The target matrix needs to have the same
   * dimensions as this matrix.
   *
   * @param that The matrix to divide with.
   * @return A new matrix containing the divided values.
   */
  def @/(that: Mat): Mat = {
    var ret = new SimpleMatrix(sm.getMatrix.numRows, that.sm.getMatrix.numCols)
    CommonOps.elementDiv(sm.getMatrix(), that.sm.getMatrix(), ret.getMatrix)
    new Mat(ret)
  }

  /**
   * Subtracts a matrix from this matrix.
   *
   * @param that The matrix to subtract.
   * @return A new matrix.
   */
  def -(that: Mat): Mat = {
    new Mat(this.sm.minus(that.sm))
  }

  /**
   * Adds a matrix to this matrix.
   *
   * @param that The matrix to add.
   * @return A new matrix.
   */
  def +(that: Mat): Mat = {
    new Mat(this.sm.plus(that.sm))
  }

  /**
   * Applies any function to all values of a matrix.
   *
   * @param f The function to apply.
   * @return A new matrix containing the transformed values.
   */
  def applyFunc(f: Double => Double): Mat = {
    var ret = new SimpleMatrix(sm.getMatrix.numRows, sm.getMatrix.numCols)
    var i = 0
    while (i < ret.numRows()) {
      var j = 0
      while (j < ret.numCols()) {
        ret.set(i, j, f(sm.get(i, j)))
        j += 1
      }
      i += 1
    }
    new Mat(ret)
  }

  /**
   * Extracts the diagonal.
   *
   * @return A new matrix containing the diagonal.
   */
  def extractDiag = {
    new Mat(sm.extractDiag())
  }

  /**
   * Checks if two matrices are identical (within a given tolerance)
   *
   * @param a The matrix to check.
   * @param tol The tolerance.
   * @return Boolean that is true if they are identical.
   */
  def isIdentical(a: Mat, tol: Double) = {
    sm.isIdentical(a.sm, tol)
  }

  /**
   * Returns true if the matrix contains only positive elements.
   *
   * @return True if the matrix only contains positive elements.
   */
  def isPositive = {
    var isPos = true
    var i = 0
    while (i < nRows) {
      var j = 0
      while (j < nCols) {
        isPos = isPos && (sm.get(i, j) >= 0.0)
        j += 1
      }
      i += 1
    }
    isPos
  }

  /**
   * Gets the most negative number in the matrix, along with its index.
   *
   * @return
   */
  def getMostNegative = {
    var value = Double.MaxValue
    var row = 0
    var col = 0
    var i = 0
    while (i < nRows) {
      var j = 0
      while (j < nCols) {
        if (sm.get(i, j) < value) {
          value = sm.get(i, j)
          row = i
          col = j
        }
        j += 1
      }
      i += 1
    }
    (value, row, col)
  }

  /**
   * Checks if the matrix has any uncountable values like NaN or inf.
   *
   * @return True if it has any.
   */
  def hasUncountable = {
    sm.hasUncountable
  }

  /**
   * Copy matrix B into this matrix at location (insertRow, insertCol).
   *
   * @param insertRow First row the matrix is to be inserted into.
   * @param insertCol First column the matrix is to be inserted into.
   * @param B The matrix that is being inserted.
   */
  def insertIntoThis(insertRow: Int, insertCol: Int, B: Mat) = {
    CommonOps.insert(B.sm.getMatrix(), sm.getMatrix, insertRow, insertCol);
  }

  /**
   * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
   * Both matrix must have the same dimension.
   *
   * @param a The matrix whose value this matrix is being set to.
   */
  def set(a: Mat) = {
    sm.getMatrix.set(a.sm.getMatrix())
  }

  /**
   * Sets all the elements in this matrix equal to the specified value.
   *
   * @param value
   */
  def set(value: Double) = {
    CommonOps.fill(sm.getMatrix, value)
  }

  /**
   * Sets all elements in the matrix to zero.
   */
  def toZero = {
    sm.getMatrix.zero
  }

  /**
   * Gets the norm 2 of this matrix.
   *
   * @return
   */
  def norm2 = {
    NormOps.normP2(sm.getMatrix)
  }

  /**
   * Computes either the vector p-norm or the induced matrix p-norm depending on A being
   * a vector or a matrix respectively.
   *
   * @param p
   * @return
   */
  def normP(p: Double) = {
    NormOps.normP(sm.getMatrix, p)
  }

  /**
   * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
   * system  Ax=b.  A value near one indicates that it is a well conditioned matrix.
   *
   * @return The condition number.
   */
  def conditionP2 = {
    NormOps.conditionP2(sm.getMatrix)
  }

  /**
   * Gets the condition p = 1 number of a matrix.
   *
   * @param p
   * @return
   */
  def conditionP(p: Double) = {
    NormOps.conditionP(sm.getMatrix, p)
  }

  /**
   * Normalizes the matrix such that the Frobenius norm is equal to one.
   */
  def normalizeF = NormOps.normalizeF(sm.getMatrix)

  /**
   * Normalizes the matrix such that the Frobenius norm is equal to one.
   *
   * @return
   */
  def fastNormF = NormOps.fastNormF(sm.getMatrix)

  /**
   * Computes the p=1 norm. If A is a matrix then the induced norm is computed.
   *
   * @return
   */
  def normP1 = NormOps.normP1(sm.getMatrix)

  /**
   * Computes the p=2 norm. If A is a matrix then the induced norm is computed
   *
   * @return
   */
  def normP2 = NormOps.normP2(sm.getMatrix)

  /**
   * Computes the p=2 norm. If A is a matrix then the induced norm is computed.
   * This implementation is faster, but more prone to buffer overflow or underflow problems.
   *
   * @return
   */
  def fastNormP2 = NormOps.fastNormP2(sm.getMatrix)

  /**
   * Computes the p = Inf norm.
   *
   * @return
   */
  def normPInf = NormOps.normPInf(sm.getMatrix)

  /**
   * Computes the norm F.
   *
   * @return
   */
  def normF = NormOps.normF(sm.getMatrix)

  /**
   * Computes induced P1.
   *
   * @return
   */
  def inducedP1 = NormOps.inducedP1(sm.getMatrix)

  /**
   * Computes induced P2.
   *
   * @return
   */
  def inducedP2 = NormOps.inducedP2(sm.getMatrix)

  /**
   * Computes induced P = Inf.
   *
   * @return
   */
  def inducedPInf = NormOps.inducedPInf(sm.getMatrix)

  /**
   * Solves the equation system C = A \ b.
   *
   * @param b
   * @return
   */
  def \(b: Mat): Mat = new Mat(this.sm.solve(b.sm))

  /**
   * Transposes the matrix.
   *
   * @return A new matrix containing the transposed values.
   */
  def transpose = {
    var ret = new SimpleMatrix(sm.getMatrix.numCols, sm.getMatrix.numRows)
    CommonOps.transpose(sm.getMatrix, ret.getMatrix)

    new Mat(ret)
  }

  /**
   * Transposes the matrix.
   *
   * @return A new matrix containing the transposed values.
   */
  def unary_~ = {
    transpose
  }

  /**
   * Computes the trace of the matrix.
   *
   * @return
   */
  def trace() = {
    sm.trace
  }

  /**
   * Computes the determinant of the matrix.
   *
   * @return
   */
  def det() = {
    sm.determinant
  }

  // TODO Take look here
  //def cond() = scalaSci.math.LinearAlgebra.LinearAlgebra.cond(toDoubleArray())

  /**
   * Computes the rank of the matrix.
   *
   * @return
   */
  def rank() = sm.svd(true).rank()

  /**
   * Performs a matrix inversion operation on the specified matrix and stores
   * the results in the same matrix.
   * If the algorithm could not invert the matrix then false is returned. If it returns true
   * that just means the algorithm finished. The results could still be bad because
   * the matrix is singular or nearly singular.
   * Note that results are stored in-place.
   *
   * @return False if the matrix could not be inverted.
   */
  def invertInPlace: Boolean = {
    CommonOps.invert(sm.getMatrix)
  }

  /**
   * Computes the inverse of this matrix.
   *
   * @return A new matrix containing the inverse.
   */
  def inv(): Mat = {
    var result = new Mat(nRows, nCols)
    CommonOps.invert(sm.getMatrix, result.getMatrix)
    result
  }

  /**
   * Computes the Moore-Penrose pseudo-inverse.
   * Ff one needs to solve a system where m != n, then  solve should be used instead since it
   * will produce a more accurate answer faster than using the pinv.
   *
   * @return A new matrix that is the inverse of this matrix.
   */
  def pinv: Mat = {
    var result = new Mat(nCols, nRows)
    CommonOps.pinv(sm.getMatrix, result.getMatrix)
    result
  }

  /**
   * Computes the Kronecker product between this matrix and the provided B matrix:
   *      C = kron(A,B)
   *
   * @param B The right matrix in the operation. Not modified.
   * @return Kronecker product between this matrix and B.
   */
  def kron(B: Mat) = {
    var C = new DenseMatrix64F(sm.getMatrix.numRows * B.sm.numRows(), sm.getMatrix.numCols * B.sm.numCols())
    CommonOps.kron(sm.getMatrix, B.sm.getMatrix, C)

    new Mat(SimpleMatrix.wrap(C))
  }

  /**
   * Computes LU decomposition.
   *
   * @return Tuple containing the lower and upper matrixes of the decomposition.
   */
  def LU = {
    val dm = sm.getMatrix // get the DenseMatrix

    val lusolver = new LUDecompositionAlt
    lusolver.decompose(dm)
    var lower = new DenseMatrix64F(nRows, nCols)
    var upper = new DenseMatrix64F(nRows, nCols)
    (new Mat(new SimpleMatrix(lusolver.getLower(lower))), new Mat(new SimpleMatrix(lusolver.getUpper(upper))))
  }

  /**
   * Computes Cholesky LDL decomposition.
   *
   * @return Triple containing (D, L, V)
   */
  def CholeskyLDL = {
    val dm = sm.getMatrix // get the DenseMatrix

    val choleskyDecLDL = new CholeskyDecompositionLDL
    choleskyDecLDL.decompose(dm)

    val DChol = choleskyDecLDL.getD // Diagonal elements of the diagonal D matrix
    val LChol = choleskyDecLDL.getL
    val VChol = choleskyDecLDL._getVV

    (DChol, LChol, VChol)
  }

  /**
   * Computes Cholesky block decomposition.
   *
   * @return Decomposition block.
   */
  def CholeskyBlock = {
    val dm = sm.getMatrix // get the DenseMatrix

    val lower = false
    val choleskyDecBlock = new CholeskyDecompositionBlock64(lower)
    choleskyDecBlock.decompose(dm)

    val TChol = choleskyDecBlock.getT(dm)

    TChol
  }

  /**
   * Reference to QR function.
   *
   * @return
   */
  private def QRHouseholder = QR

  /*
    var N = 100
    var A = scalaSci.EJML.StaticMathsEJML.rand0(N, N)
    var 
   */
  /**
   * Computes QR decomposition.
   *
   * @return Tuple containing Q and R matrices.
   */
  def QR = {
    val QRDHS = new QRDecompositionHouseholder
    QRDHS.decompose(this.sm.getMatrix)
    var Qp = new DenseMatrix64F(nRows, nCols)
    var Rp = new DenseMatrix64F(nRows, nCols)
    (new Mat(new SimpleMatrix(QRDHS.getQ(Qp, false))), new Mat(new SimpleMatrix(QRDHS.getR(Rp, false))))
  }

  /**
   * QR decomposition can be used to solve for systems.  However, this is not as computationally efficient
   * as LU decomposition and costs about 3n<sup>2</sup> flops.
   * It solve for x by first multiplying b by the transpose of Q then solving for the result.
   *    QRx = b
   *    Rx = Q^T b
   *
   * @param B
   * @return
   */
  def QRHouseholderSolve(B: Mat) = {
    val A = sm.getMatrix // get the DenseMatrix

    val QRHHSolver = new LinearSolverQrHouse
    QRHHSolver.setA(A)
    var X = new DenseMatrix64F(nRows, nCols)

    QRHHSolver.solve(B.sm.getMatrix, X)

    new Mat(new SimpleMatrix(X))
  }

  /**
   * Computes eigenvectors and eigenvalues.
   *
   * @return A tuple containing eigenvectors in a matrix and eigenvalues.
   */
  def eigEJML() = {
    val ejmlEigs = this.sm.eig()
    val numEvals = ejmlEigs.getNumberOfEigenvalues // get number of eigenvalues
    var eigenVec = ejmlEigs.getEigenVector(0).getMatrix
    val eigenVecDim = eigenVec.numRows // dimension of eigenvectors
    var V = Array.ofDim[Double](numEvals, eigenVecDim) // construct the matrix to fill the eigenvectors
    var D = new Array[Double](numEvals)
    var ev = 0
    while (ev < numEvals) {
      D(ev) = ejmlEigs.getEigenvalue(ev).real
      eigenVec = ejmlEigs.getEigenVector(ev).getMatrix
      var k = 0
      while (k < eigenVecDim) {
        V(ev)(k) = eigenVec.get(k, 0)
        k += 1
      }
      ev += 1
    }

    (new Mat(V), D)
  }

  /**
   * Computes single value decomposition.
   *
   * @return
   */
  def svd() = {
    sm.svd()
  }

  /**
   * Computes single value decomposition. Compact if wished.
   *
   * @param compact Computes compact form.
   * @return
   */
  def svd(compact: Boolean) = {
    sm.svd(compact)
  }

  /**
   * Computes eigenvalues.
   *
   * @return
   */
  def eig() = {
    sm.eig()
  }

  /**
   * Sums up all elements of this matrix.
   */
  def sum = {
    sm.elementSum
  }

  /**
   * Creates a new matrix with absolute values from the current one.
   *
   * @return A new matrix contatining only positive values.
   */
  def abs = {
    Mat.abs(this)
  }

  /**
   * Sums up all elements in a row.
   *
   * @return
   */
  def sumR = {
    Mat.sumR(this)
  }

  /**
   * Sums up all elements in a column.
   *
   * @return
   */
  def sumC = {
    Mat.sumC(this)
  }

  /**
   * Produces nice string that displays the matrix.
   *
   * @return
   */
  def toNiceString(decimals: Int) = {
    val indents = Array.ofDim[Int](nCols)
    val regex = """(-?\d+)\.""".r
    for {
      i <- 0 until nRows
      j <- 0 until nCols
    } {
      indents(j) = Math.max(indents(j), regex.findFirstIn(sm.get(i, j).toString).getOrElse("").length + decimals)
    }
    val matrixString = for (i <- 0 until nRows) yield {
      for (j <- 0 until nCols) yield {
        val str = ("%." + decimals + "f").format(sm.get(i, j))
        (" " * Math.max(0, indents(j) - str.length)) + str
      }
    }.mkString(", ")

    s"Matrix of size $nRows x $nCols: \n[ " + matrixString.mkString("\n  ") + " ]"
  }

  /**
   * Renders the matrix as one line string.
   *
   * @return
   */
  def toFlatString = {
    s"Matrix of dimension $dim (size $nRows x $nCols): [ " + this.data.mkString(", ") + " ]"
  }

  /**
   * Prints matrix to console.
   */
  def print = {
    println(toNiceString(3))
  }
}

/**
 * Companion object for fast matrix creation.
 *
 * @author Dominik
 *
 */
object Mat {
  /**
   * Creates a matrix from an array of doubles. The first two values denote rows and cols respectively,
   * the others are the values put into the matrix.
   *
   * @param values
   * @return
   */
  def apply(values: Double*) = {
    val nrows = values(0).toInt //   number of rows
    val ncols = values(1).toInt // number of cols
    val dvalues = values.toArray
    var cpos = 2 // current position in array
    var sm = new Mat(nrows, ncols) // create a Mat
    for (r <- 0 until nrows)
      for (c <- 0 until ncols) {
        sm(r, c) = values(cpos) // copy value
        cpos += 1
      }

    sm // return the constructed matrix
  }

  /**
   * Creates a new matrix of size nRows x nCols.
   *
   * @param nRows Number of rows.
   * @param nCols Number of columns.
   * @return The newly created matrix.
   */
  def apply(nRows: Int, nCols: Int) = {
    new Mat(nRows, nCols)
  }

  /**
   * Creates a diagonal matrix of size "size x size". The diagonal elements are set to 1.
   *
   * @param size The size of the matrix.
   * @return A new matrix with diagonal elements equals 1.
   */
  def diag(size: Int) = {
    val ret = new Mat(size, size)
    for (i <- 0 until size) ret.sm.set(i, i, 1)
    ret
  }

  /**
   * Creates a matrix, filled with random double values up to max.
   *
   * @param nRows Number of rows.
   * @param nCols Number of columns.
   * @param max The maximal double to be contained in the matrix.
   * @return
   */
  def random(nRows: Int, nCols: Int, max: Double) = {
    val m = new Mat(nRows, nCols)
    for {
      r <- 0 until nRows
      c <- 0 until nCols
    } {
      m(r, c) = max * math.random
    }
    m
  }

  /**
   * Creates a matrix, filled with the value specified.
   *
   * @param nRows Number of rows.
   * @param nCols Number of columns.
   * @param value The value to fill the matrix with.
   * @return The new matrix.
   */
  def fromValue(nRows: Int, nCols: Int, value: Double) = {
    val m = new Mat(nRows, nCols)
    for {
      r <- 0 until nRows
      c <- 0 until nCols
    } {
      m(r, c) = value
    }
    m
  }

  /**
   * Calculates a columnwise sum.
   *
   * @param matr
   * @return
   */
  def sumC(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var sm = 0.0
    var res = new Array[Double](Ncols)
    var ccol = 0
    while (ccol < Ncols) {
      sm = 0.0
      var crow = 0
      while (crow < Nrows) {
        sm += matr(crow, ccol)
        crow += 1
      }
      res(ccol) = sm

      ccol += 1
    }
    res
  }

  /**
   * Calculates a columnwise mean
   *
   * @param matr
   * @return
   */
  def meanC(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var sm = 0.0
    var res = new Array[Double](Ncols)
    var ccol = 0
    while (ccol < Ncols) {
      sm = 0.0
      var crow = 0
      while (crow < Nrows) {
        sm += matr(crow, ccol)
        crow += 1
      }
      res(ccol) = sm / Nrows

      ccol += 1
    }
    res
  }

  /**
   * Calculates a columnwise product.
   *
   * @param matr
   * @return
   */
  def prodC(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var pd = 1.0
    var res = new Array[Double](Ncols)
    var ccol = 0
    while (ccol < Ncols) {
      pd = 1.0
      var crow = 0
      while (crow < Nrows) {
        pd *= matr(crow, ccol)
        crow += 1
      }
      res(ccol) = pd

      ccol += 1
    }
    res
  }

  /**
   * Calculates a columnwise min.
   *
   * @param matr
   * @return
   */
  def minC(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var res = new Array[Double](Ncols)
    var ccol = 0
    while (ccol <= Ncols - 1) {
      var mn = matr(0, ccol) // keeps the running min element
      var crow = 0
      while (crow <= Nrows - 1) {
        var tmp = matr(crow, ccol)
        if (tmp < mn) mn = tmp
        crow += 1
      }
      res(ccol) = mn // min element for the ccol column
      ccol += 1
    }
    res
  }

  /**
   * Calculates a columnwise max.
   *
   * @param matr
   * @return
   */
  def maxC(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var res = new Array[Double](Ncols)
    var ccol = 0
    while (ccol < Ncols) {
      var mx = matr(0, ccol) // keeps the running max element
      var crow = 1
      while (crow < Nrows) {
        var tmp = matr(crow, ccol)
        if (tmp > mx) mx = tmp
        crow += 1
      }
      res(ccol) = mx // max element for the ccol column

      ccol += 1
    }
    res
  }

  /**
   * Calculates a rowwise sum.
   *
   * @param matr
   * @return
   */
  def sumR(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var sm = 0.0
    var res = new Array[Double](Nrows) // sum for all rows
    var crow = 0
    while (crow < Nrows) {
      sm = 0.0
      var ccol = 0
      while (ccol < Ncols) { // sum across column
        sm += matr(crow, ccol)
        ccol += 1
      }
      res(crow) = sm
      crow += 1
    }
    res
  }

  /**
   * Calculates a rowwise mean.
   *
   * @param matr
   * @return
   */
  def meanR(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var sm = 0.0
    var res = new Array[Double](Nrows) // mean for all rows
    var crow = 0
    while (crow < Nrows) {
      sm = 0.0
      var ccol = 0
      while (ccol < Ncols) { // sum across column
        sm += matr(crow, ccol)
        ccol += 1
      }
      res(crow) = sm / Ncols

      crow += 1
    }
    res
  }

  /**
   * Calculates a rowwise product.
   *
   * @param matr
   * @return
   */
  def prodR(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var pd = 1.0
    var res = new Array[Double](Nrows)
    var crow = 0
    while (crow < Nrows) {
      pd = 1.0
      var ccol = 0
      while (ccol < Ncols) { // product across column
        pd *= matr(crow, ccol)
        ccol += 1
      }

      res(crow) = pd
      crow += 1
    }
    res
  }

  /**
   * Calculates a rowwise min.
   *
   * @param matr
   * @return
   */
  def minR(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var res = new Array[Double](Nrows)
    var crow = 0
    while (crow < Nrows) {
      var mn = matr(crow, 0) // keeps the running min element
      var ccol = 1
      while (ccol < Ncols) {
        var tmp = matr(crow, ccol)
        if (tmp < mn) mn = tmp
        ccol += 1
      }
      res(crow) = mn // min element for the crow row
      crow += 1
    }
    res
  }

  /**
   * Calculates a rowwise max.
   *
   * @param matr
   * @return
   */
  def maxR(matr: Mat): Array[Double] = {
    var Nrows = matr.nRows; var Ncols = matr.nCols
    var res = new Array[Double](Nrows)
    var crow = 0
    while (crow < Nrows) {
      var mx = matr(crow, 0) // keeps the running max element
      var ccol = 1
      while (ccol < Ncols) {
        var tmp = matr(crow, ccol)
        if (tmp > mx) mx = tmp
        ccol += 1
      }
      res(crow) = mx // max element for the ccol column
      crow += 1
    }
    res
  }

  /**
   * Converts an angle measured in radians to an approximately equivalent angle measured in degrees
   * for all matrix values.
   *
   * @param v
   * @return
   */
  def toDegrees(v: Mat): Mat = {
    var Nrows = v.nRows; var Ncols = v.nCols;
    var om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.toDegrees(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Converts an angle measured in degrees to an approximately equivalent angle measured in radians
   * for all matrix values.
   *
   * @param v
   * @return
   */
  def toRadians(v: Mat): Mat = {
    val Nrows = v.nRows; val Ncols = v.nCols;
    val om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.toRadians(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Takes the square root for all matrix values.
   *
   * @param v
   * @return
   */
  def sqrt(v: Mat): Mat = {
    var Nrows = v.nRows; var Ncols = v.nCols;
    var om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.sqrt(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Calculates absolute value of all elements of a matrix.
   *
   * @param v The matrix to calculate the absolute value.
   * @return A new matrix with only positive elements.
   */
  def abs(v: Mat): Mat = {
    var Nrows = v.nRows; var Ncols = v.nCols;
    var om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = math.abs(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Finds non-zero elements in the matrix. Returns in a matrix containing indices:
   * [ row + col * n, row, col ]
   *
   * @param M Matrix to perform operation upon.
   * @return The matrix with the indices as described above.
   */
  def find(M: Mat) = {
    var n = M.nRows
    var m = M.nCols
    // find number of nonzero elements
    var no = 0
    var xi = 0
    while (xi < n) {
      var yi = 0
      while (yi < m) {
        if (M(xi, yi) != 0.0)
          no += 1
        yi += 1
      }
      xi += 1
    }

    // build return vector
    var indices = Array.ofDim[Int](no, 3)
    var i = 0
    var col = 0
    while (col < m) {
      var row = 0
      while (row < n) {
        if (M(row, col) != 0.0) {
          // nonzero element found
          // put element position into return column vector
          indices(i)(0) = row + col * n
          indices(i)(1) = row
          indices(i)(2) = col
          i += 1
        }
        row += 1
      }
      col += 1
    }

    indices
  }

  /**
   * Converts a dense matrix to a double array.
   *
   * @param x
   * @return
   */
  def DenseMatrixToDoubleArray(x: DenseMatrix64F) = {
    var nr = x.numRows
    var nc = x.numCols
    var xa = Array.ofDim[Double](nr, nc)
    var row = 0; var col = 0
    while (row < nr) {
      while (col < nc) {
        xa(row)(col) = x.get(row, col)
        col += 1
      }
      row += 1
    }
    xa
  }

  /**
   * Floors all values in the matrix.
   *
   * @param v
   * @return
   */
  def floor(v: Mat): Mat = {
    var Nrows = v.nRows; var Ncols = v.nCols;
    var om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.floor(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Rounds all values in the matrix.
   *
   * @param v
   * @return
   */
  def round(v: Mat): Mat = {
    var Nrows = v.nRows; var Ncols = v.nCols;
    var om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.round(v(i, j))
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Pows all elements in the matrix with a given exponent.
   *
   * @param v
   * @param exponent
   * @return
   */
  def pow(v: Mat, exponent: Double): Mat = {
    val Nrows = v.nRows; val Ncols = v.nCols;
    val om = new Mat(Nrows, Ncols)
    var i = 0; var j = 0;
    while (i < Nrows) {
      j = 0
      while (j < Ncols) {
        om(i, j) = java.lang.Math.pow(v(i, j), exponent)
        j += 1
      }
      i += 1
    }
    om
  }

  /**
   * Element sum of matrix M.
   *
   * @param m
   * @return
   */
  def sum(m: Mat) = {
    m.sm.elementSum
  }
}

trait RowColMatSelector

object :: extends RowColMatSelector
