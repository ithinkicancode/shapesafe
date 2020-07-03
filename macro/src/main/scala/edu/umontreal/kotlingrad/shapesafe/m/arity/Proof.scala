package edu.umontreal.kotlingrad.shapesafe.m.arity

import scala.language.higherKinds

/**
  * is a view class
  */
trait Proof extends Serializable {

  def self: Operand

  type Out <: Arity

  def out: Out
}

object Proof {

  trait Unsafe extends Proof {

    final type Out = Arity.Unknown.type
    final def out: Out = Arity.Unknown
  }

  trait Out_=[+O <: Arity] extends Proof {
    type Out <: O
  }

  // TODO: type Out should be a parameter instead of a dependent type
  //  otherwise inferring Out will require Lazy
  trait Invar[S] extends Out_=[Arity.Const[S]] {

    type SS = S
  } // can't use type alias? really?

}
