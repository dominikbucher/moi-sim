/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.macr

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.internal.Names

/*object Trackable extends ReflectionUtils {
  def track[A](a: A): A = macro track_impl[A]

  def track_impl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]) = {
    import c.universe._

    val wrapped = weakTypeOf[A]
    //val f = Select(reify(Predef).tree, "println")
    val f = Select(This(tpnme.EMPTY), "track")
    val anon = newTypeName(c.fresh("Tracked" + wrapped.typeSymbol.name.decoded + "$"))
    val wrapper2 = newTypeName(c.fresh("TrackedH" + wrapped.typeSymbol.name.decoded + "$$"))

    val members = wrapped.members.collect {
      case m: MethodSymbol if m.isGetter && m.name.decoded.startsWith("_") =>
        val newName = m.name.decoded.substring(1, m.name.decoded.length())
        val tmp = m.paramss.foldLeft(Select(This(tpnme.EMPTY), m.name): Tree)((prev, params) =>
          Apply(prev, params.map(p => Ident(p.name))))
        // Setter
        c.universe.ValDef(
          Modifiers(Flag.MUTABLE),
          newTermName(newName),
          Ident(m.typeSignature.typeSymbol.name),
          Select(This(tpnme.EMPTY), m.name))
    }.toList
    val setters = wrapped.declarations.collect {
      case m: MethodSymbol if m.isSetter && m.name.decoded.startsWith("_") =>
        val newName = m.name.decoded.substring(1, m.name.decoded.length())
        val getterName = m.name.decoded.substring(0, m.name.decoded.length() - 2)
        val tmpName = newTermName(c.fresh)
        // Setter
        DefDef(
          Modifiers(),
          newTermName(newName).encodedName,
          m.typeParams.map(TypeDef(_)),
          m.paramss.map(_.map(ValDef(_))),
          TypeTree(m.returnType),
          Block(
            //Apply(f, c.literal("Calling: " + newName).tree :: Nil),
            //            m.paramss.flatMap(params =>
            //              params.flatMap(p => )),
            ValDef(Modifiers(Flag.MUTABLE), tmpName, Ident(m.paramss.head.head.typeSignature.typeSymbol), Select(This(tpnme.EMPTY), newTermName(getterName).encoded)),
            m.paramss.foldLeft(Select(This(tpnme.EMPTY), m.name): Tree)((prev, params) =>
              Apply(prev, params.map(p => Ident(p.name)))),
            Apply(f, c.literal(getterName).tree :: Ident(tmpName) :: Select(This(tpnme.EMPTY), newTermName(getterName).encoded) :: Nil)))
    }.toList
    val getters = wrapped.declarations.collect {
      case m: MethodSymbol if m.isGetter && m.name.decoded.startsWith("_") =>
        val newName = m.name.decoded.substring(1, m.name.decoded.length())
        // Setter
        DefDef(
          Modifiers(),
          newTermName(newName).encodedName,
          m.typeParams.map(TypeDef(_)),
          m.paramss.map(_.map(ValDef(_))),
          TypeTree(m.returnType),
          //          Block(
          //            Apply(f, c.literal("Calling: " + newName).tree :: Nil),
          m.paramss.foldLeft(Select(This(tpnme.EMPTY), m.name): Tree)((prev, params) =>
            Apply(prev, params.map(p => Ident(p.name))))) //)
    }.toList
    //c.info(c.enclosingPosition, "Generated setters: " + setters.mkString(", "), true)
    //    c.info(c.enclosingPosition, "Generated getters: " + getters.mkString(", "), true)
    //    val methods = wrapped.declarations.collect {
    //      case m: MethodSymbol if !m.isConstructor && m.name.decoded.startsWith("_") => DefDef(
    //        Modifiers(Flag.OVERRIDE),
    //        m.name,
    //        m.typeParams.map(TypeDef(_)),
    //        m.paramss.map(_.map(ValDef(_))),
    //        TypeTree(m.returnType),
    //        Block(
    //          Apply(f, c.literal("Calling: " + m.name.decoded).tree :: Nil),
    //          m.paramss.foldLeft(Select(a.tree.duplicate, m.name): Tree)((prev, params) =>
    //            Apply(prev, params.map(p => Ident(p.name))))))
    //    }.toList

    val newconstructor = DefDef(
      Modifiers(), nme.CONSTRUCTOR, Nil, Nil :: Nil, TypeTree(),
      Block(
        Apply(
          Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR), Nil) :: Nil,
        c.literalUnit.tree))
    val someMethod = DefDef(
      Modifiers(), newTermName("myMethod"), Nil, Nil :: Nil, TypeTree(),
      Block(
        Apply(f, c.literal("Calling: myMethod").tree :: Nil) :: Nil,
        c.literalUnit.tree))
    //    c.info(c.enclosingPosition, "smM: " + show(someMethod), true)

    val aasdf = c.Expr(Block(
      ClassDef(Modifiers(), anon, Nil,
        Template(
          List(Ident(wrapped.typeSymbol.name), Ident(newTypeName("TrackChange"))),
          emptyValDef,
          newconstructor :: /*someMethod ::*/ setters ::: getters /*::: members */ )),
      ClassDef(
        Modifiers(Flag.FINAL), wrapper2, Nil,
        Template(Ident(anon) :: Nil, emptyValDef, constructor(c.universe) :: Nil)),
      Apply(Select(New(Ident(wrapper2)), nme.CONSTRUCTOR), Nil)))
    //c.info(c.enclosingPosition, "smM: " + show(aasdf), true)
    aasdf
  }
}*/

object ChangeModelMacros extends ReflectionUtils {
  def trackable[T](value: Any) = macro trackable_impl[T]

  def trackable_impl[T: c.WeakTypeTag](c: Context)(value: c.Expr[Any]): c.Expr[T] = {
    import c.universe._

    val typeOfT = weakTypeOf[T]
    val clazz = c.enclosingClass.symbol.typeSignature
    println("enclosing method: " + c.enclosingMethod)
    case class TrackedProperty[T](i: T, s: String, t: Any)
    val ee = c.universe.reify(new TrackedProperty[Double](3.0, "", null))

    val e = c.Expr[T](Apply(Select(New(AppliedTypeTree(Ident("TrackedProperty"), List(Ident("A")))), nme.CONSTRUCTOR),
      List(value.tree, Literal(Constant(c.enclosingClass.symbol.name.decoded)),
        This(tpnme.EMPTY))))
    println(showRaw(ee) + " \n\n")
    c.warning(c.enclosingPosition, show(e))
    e
  }

  def setUpMessages: PropRefs = macro setUpMessages_impl

  def setUpMessages_impl(c: Context) = {
    import c.universe._

    //    var evolveMethod: DefDef = null
    //
    //    object evolveExtractor extends Traverser {
    //      override def traverse(tree: Tree): Unit = tree match {
    //        case m @ DefDef(mod, name, paramss, paramnames, leftTree, rightTree) if name.decoded.equals("evolve") =>
    //          evolveMethod = m
    //        case _ => super.traverse(tree)
    //      }
    //    }
    //
    //    evolveExtractor.traverse(c.enclosingClass)

    // c.info(c.enclosingPosition, c.enclosingClass.symbol.typeSignature.declaration(newTermName("evolve")).asMethod.toString, true)
    //c.info(c.enclosingPosition, showRaw(evolveMethod), true)

    var updatePropNames: List[String] = List()
    var applyPropNames: List[String] = List()

    object updateAndApplyExtractor extends Traverser {
      override def traverse(tree: Tree): Unit = tree match {
        case s @ Select(Ident(termName), update) if update.decoded.equals("update") => {
          //c.info(c.enclosingPosition, termName + "<-", true)
          updatePropNames = termName.decoded :: updatePropNames
        }
        case s @ Apply(Ident(termName), List()) => {
          //c.info(c.enclosingPosition, termName + "()", true)
          applyPropNames = termName.decoded :: applyPropNames
        }
        case s @ Select(Select(Ident(cls), termName), update) if update.decoded.equals("update") => {
          //c.info(c.enclosingPosition, cls + "." + termName + "<-", true)
          updatePropNames = termName.decoded :: updatePropNames
        }
        case s @ Apply(Select(Ident(cls), termName), List()) => {
          //c.info(c.enclosingPosition, cls + "." + termName + "()", true)
          applyPropNames = termName.decoded :: applyPropNames
        }
        case _ => super.traverse(tree)
      }
    }

    updateAndApplyExtractor.traverse(c.enclosingClass)
    //c.info(c.enclosingPosition, showRaw(reify {PropRefs(List("a", "b", "c"), List("e", "f"))}), true)

    val updatePropList = updatePropNames.removeDuplicates.map(pn => c.Expr[Any](Literal(Constant(pn))).tree)
    val applyPropList = applyPropNames.removeDuplicates.map(pn => c.Expr[Any](Literal(Constant(pn))).tree)

    //c.Expr(reify(println(c.Expr[Any](Literal(Constant("yass!"))).splice)).tree)
    c.Expr[PropRefs](Apply(Select(Ident(newTermName("PropRefs")), newTermName("apply")), List(Apply(Select(Ident(newTermName("List")), newTermName("apply")), updatePropList), Apply(Select(Ident(newTermName("List")), newTermName("apply")), applyPropList))))
  }

  //def instantiateSkeleton[T <: t: ClassManifest](stateName: String): T = macro instantiateSkeleton_impl[T]

  def instantiateSkeleton_impl[T: c.WeakTypeTag](c: Context)(stateName: c.Expr[String]): c.Expr[T] = {
    import c.universe._

//    trait t
//    var states = collection.mutable.Map.empty[String, t]
//
//    c.info(c.enclosingPosition, showRaw(reify {
//      val v = { final class anon(val name: String) extends t {}; new anon("name") }
//      states += ("name" -> v);
//      v
//    }), true)

    val typeOfT = weakTypeOf[T]
    val claz = typeOfT.typeSymbol.name
    val clazz = c.enclosingClass.symbol.typeSignature

    val constructor = DefDef(Modifiers(), nme.CONSTRUCTOR, Nil,
      List(List(
        ValDef(
          Modifiers(Flag.PARAM | scala.reflect.internal.Flags.PARAMACCESSOR.toLong.asInstanceOf[FlagSet]),
          newTermName("name"),
          Ident(newTypeName("String")), EmptyTree))),
      TypeTree(),
      Block(
        Apply(
          Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR), Nil) :: Nil,
        c.literalUnit.tree))

    val anonClass = ClassDef(Modifiers(Flag.FINAL), newTypeName("$anon"), Nil,
      Template(List(Ident(claz)), emptyValDef, List(
        ValDef(Modifiers(scala.reflect.internal.Flags.PARAMACCESSOR.toLong.asInstanceOf[FlagSet]),
          newTermName("name"),
          Ident(newTypeName("String")), EmptyTree),
        constructor/*,
        ValDef(Modifiers(),
          newTermName("name"), TypeTree(), stateName.tree)*/)))

    val e = c.Expr[T](Block(List(ValDef(Modifiers(), newTermName("v"), TypeTree(), Block(List(anonClass), Apply(Select(New(Ident(newTypeName("$anon"))), nme.CONSTRUCTOR), List(stateName.tree)))),
      Apply(Select(Ident(newTermName("states")), newTermName("$plus$eq")),
        List(Apply(Select(Apply(Select(Ident(newTermName("Predef")), newTermName("any2ArrowAssoc")), List(stateName.tree)),
          newTermName("$minus$greater")), List(Ident(newTermName("v"))))))), Ident(newTermName("v"))))
//    c.info(c.enclosingPosition, show(e), true)
    e
  }
}

case class PropRefs(writeProps: List[String], readProps: List[String])