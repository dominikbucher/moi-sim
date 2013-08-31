/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.macr

/**
 * Some reflection utils to get class constructors / accessors / ...
 * Method not used as of 2013-08.
 */
trait ReflectionUtils {
  import scala.reflect.api.Universe

  def constructor(u: scala.reflect.api.Universe) = {
    import u._

    DefDef(
      Modifiers(),
      nme.CONSTRUCTOR,
      Nil,
      Nil :: Nil,
      TypeTree(),
      Block(
        Apply(
          Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR),
          Nil)))
  }

  def accessors[A: u.WeakTypeTag](u: Universe) = {
    import u._

    u.weakTypeOf[A].declarations.collect {
      case acc: MethodSymbol if acc.isCaseAccessor => acc
    }.toList
  }

  def printfTree(u: Universe)(format: String, trees: u.Tree*) = {
    import u._

    Apply(
      Select(reify(Predef).tree, "printf"),
      Literal(Constant(format)) :: trees.toList)
  }
}