package org.shapesafe.core.arity.binary

import com.tribbloids.graph.commons.util.HasOuter
import org.shapesafe.core.arity.LeafArity.Const
import org.shapesafe.core.arity.ProveArity.|-<
import org.shapesafe.core.arity.Utils.Op
import org.shapesafe.core.arity._
import org.shapesafe.core.debugging.PeekInfo
import singleton.ops.+

import scala.language.implicitConversions

class Op2[
    ??[X1, X2] <: Op
](
    implicit
    sh: Utils.IntSh[??]
) extends Op2Like {

  trait DO[A1, A2]

  case class On[
      A1 <: Arity,
      A2 <: Arity
  ](
      a1: A1,
      a2: A2
  ) extends ArityConjecture // TODO: can this be VerifiedArity?
      with HasOuter {

    def outer: Op2.this.type = Op2.this

    override lazy val runtimeArity: Int = sh.apply(a1.runtimeArity, a2.runtimeArity).getValue

    override type Out = DO[a1.Out, a2.Out]
  }

  override def on(a1: ArityAPI, a2: ArityAPI): On[a1._Arity, a2._Arity] = On(a1.arity, a2.arity)

  final override type CannotI[I1 <: PeekInfo, I2 <: PeekInfo] =
    PeekInfo.noCanDo.T + PeekInfo.nonExisting.T + I1#_Peek + I2#_Peek
}

trait Op2_Imp0 {

  implicit def unchecked[
      A1 <: Arity,
      A2 <: Arity,
      O <: ProveArity.Term,
      ??[X1, X2] <: Op
  ](
      implicit
      domain: UncheckedDomain[A1, A2, O],
      sh: Utils.IntSh[??]
  ) = {
    domain.forOp2[??]
  }
}

object Op2 extends Op2_Imp0 {

  implicit def invar[
      A1 <: Arity,
      A2 <: Arity,
      S1,
      S2,
      ??[X1, X2] <: Op
  ](
      implicit
      bound1: A1 |-< Const[S1], // TODO: make it similar to unsafe
      bound2: A2 |-< Const[S2],
      lemma: S1 ?? S2
  ) = {
    val domain = InvarDomain[A1, A2, S1, S2]()(bound1, bound2)

    domain.forOp2[??]
  }

//  def apply[
//      ??[X1, X2] <: Op,
//      A1 <: ArityCore,
//      A2 <: ArityCore
//  ](
//      a1: A1,
//      a2: A2
//  )(
//      implicit
//      sh: Utils.IntSh[??]
//  ): Op2[??]#On[A1, A2] = {
//
//    val op2 = new Op2[??] // TODO: should be cached
//
//    op2.On(a1, a2)
//  }
}
