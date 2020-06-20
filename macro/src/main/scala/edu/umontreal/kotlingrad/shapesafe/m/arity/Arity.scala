package edu.umontreal.kotlingrad.shapesafe.m.arity

import edu.umontreal.kotlingrad.shapesafe.m.arity.Utils.Op
import edu.umontreal.kotlingrad.shapesafe.m.util.TypeTag
import shapeless.Witness
import singleton.ops.{==, Require}

import scala.language.implicitConversions

trait Arity extends Operand.Proven {

//  def numberOpt: Option[Int] // run-time

  lazy val valueStr: String = ""

  override type Out = this.type
  final override val out: Out = this

  override def toString: String = {

    this.getClass.getSimpleName + ": " + valueStr
  }
}

object Arity {

  import Witness._

  implicit object Unknown extends Arity {

//    override def numberOpt: Option[Int] = None
  }

  abstract class Const[S] extends Arity with Proof.Invar[S] {

    //    implicitly[Out <:< Exact[S]]

//    def number: Int
//    final override def numberOpt: Option[Int] = Some(number)

    @transient protected[shapesafe] object internal {

      def getTTag(implicit ttag: TypeTag[SS]): TypeTag[SS] = ttag

      def proveSame[N2](implicit self: SS =:= N2): Unit = {}

      // 'prove' means happening only in compile-time
      def proveEqual[N2](implicit self: Require[SS == N2]): Unit = {

        //TODO: need run-time proof

      }

      def proveEqual(w: Lt[Int])(implicit self: Require[SS == w.T]): Unit = {

        proveEqual[w.T]

//        require(w.value == number)
      }
    }

  }

  // DO NOT directly carry Nat type! (church encoding is slow) convert to Op type first
  //  class FromSize[S <: Op](val number: Int) extends Exact[S]
  //
  //  object FromSize {
  //
  //    implicit def summon[S <: Op](implicit s: S): FromSize[S] = new FromSize[S](s.value.asInstanceOf[Int])
  //  }

  class FromOp[S <: Op]() extends Const[S]

  object FromOp {

    implicit def summon[S <: Op](implicit s: S): FromOp[S] = new FromOp[S]()
  }

  // this makes it impossible to construct directly from Int type
  class FromLiteral[S <: Int]() extends Const[S] {

//    override def number: Int = w.value
  }

  object FromLiteral {

    implicit def summon[S <: Int](implicit w: Witness.Aux[S]): FromLiteral[S] =
      new FromLiteral[S]()
  }

  def apply(w: Witness.Lt[Int]): FromLiteral[w.T] = {

    implicit val _w: Witness.Aux[w.T] = w

    FromLiteral.summon[w.T] //TODO: IDEA inspection error
  }

  lazy val _0 = Arity(0)
  type _0 = _0.type

  lazy val _1 = Arity(1)
  type _1 = _1.type

  // TODO: these doesn't work
  //  see https://stackoverflow.com/questions/62205940/when-calling-a-scala-function-with-compile-time-macro-how-to-failover-smoothly
  //  trait OfIntLike_Implicit0 { // of lower priority in resolving
  //
  //    implicit def unsafe(number: Int): OfInt_Unsafe = OfInt_Unsafe(number)
  //  }
  //
  //  object OfIntLike extends OfIntLike_Implicit0 {
  //
  //    implicit def safe(w: Lt[Int]): OfInt[w.T] = OfInt.safe(w)
  //  }
}
