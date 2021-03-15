package org.shapesafe.m

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * non singleton tightest upper bound
  * @tparam I input type
  */
trait NonSingletonTUB[I] {

  final type In = I
  type Out
}

object NonSingletonTUB {

  lazy val objectClassPath: String = {
    val fullName = this.getClass.getCanonicalName.stripSuffix("$")
    s"_root_.$fullName"
  }

  type Aux[A, Out0] = NonSingletonTUB[A] {
    type Out = Out0
  }

  case class Make[I, O]() extends NonSingletonTUB[I] {
    final type Out = O
  }

  def make[I, O]: Make[I, O] = {
//    new NonSingletonUB[I] {
//      final type Out = O
//    }

    Make[I, O]()
  }

  def apply[I]: NonSingletonTUB[I] = macro Macros.apply[I]

  final class Macros(val c: whitebox.Context) extends MWithReflection {

    import c.universe._

    def apply[A: WeakTypeTag]: Tree = {

      val tt: Type = weakTypeOf[A]
      val _tt = tt.dealias

//      val vv = viz.apply(_tt)
//      println(_tt.getClass.getCanonicalName)
//      println(vv)

      val out: Type = _tt match {

        case v: SingletonType =>
          val ttView = refl.TypeView(v)
          val baseTypes = ttView.baseTypes.map(_.self)

          val chosen = baseTypes.flatMap {
            case NoType => None
            case v @ _ => Some(v)
          }.head

          chosen

        case v @ _ =>
          v
      }

      q"_root_.org.shapesafe.m.NonSingletonTUB.make[$tt, $out]" // TODO: make the object Liftable
    }
  }
}
