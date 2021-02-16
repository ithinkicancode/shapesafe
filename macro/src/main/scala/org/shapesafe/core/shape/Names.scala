package org.shapesafe.core.shape

import org.shapesafe.core.tuple.{CanCons, CanFromStatic, TupleSystem}
import org.shapesafe.core.util.Finder.ByKey
import shapeless.{::, HList, HNil, Witness}

import scala.language.implicitConversions

trait Names extends Names.Proto {

  type Keys <: HList
  def keys: Keys
}

object Names extends TupleSystem with CanCons with CanFromStatic {

  override type UpperBound = String

  val proto = Indices // every Names can be used in place of Indices
  type Proto = proto.Impl

  final type Impl = Names

  object eye extends proto.EyeLike with Names {

    override type Keys = HNil
    override def keys = HNil
  }

  class ><[
      TAIL <: Impl,
      HEAD <: String
  ](
      override val tail: TAIL,
      val headName: HEAD
  ) extends proto.><[TAIL, ByKey[HEAD]](tail, new ByKey(headName))
      with Names {

    override type Keys = HEAD :: tail.Keys

    override def keys: Keys = headName :: tail.keys
  }

  implicit def consAlways[TAIL <: Impl, HEAD <: UpperBound]: Cons.FromFn[TAIL, HEAD, TAIL >< HEAD] = {

    Cons[TAIL, HEAD].to { (tail, head) =>
      new ><(tail, head)
    }
  }

  implicit class Infix[SELF <: Impl](self: SELF) {

    def ><(name: Witness.Lt[String]): SELF >< name.T = {

      new ><(self, name.value)
    }
  }

  implicit def toEyeInfix(s: Names.type): Infix[s.Eye] = Infix(Eye)

  trait Syntax {

    implicit def literalToNames(v: String)(
        implicit
        w: Witness.Aux[v.type]
    ): Eye >< v.type = {

      Eye >< w
    }

    implicit def literalToInfix(v: String)(
        implicit
        w: Witness.Aux[v.type]
    ): Infix[Eye >< v.type] = {

      Infix(Eye >< w)
    }
  }

  object Syntax extends Syntax
}
