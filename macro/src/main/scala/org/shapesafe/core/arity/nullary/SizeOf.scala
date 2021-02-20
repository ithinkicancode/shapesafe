package org.shapesafe.core.arity.nullary

import org.shapesafe.core.arity.ProveArity._
import org.shapesafe.core.arity.Utils.NatAsOp
import org.shapesafe.core.arity.{Arity, LeafArity, ProveArity}
import shapeless.ops.hlist
import shapeless.{HList, Nat}

// TODO: should not carry the proof
case class SizeOf[D <: HList](data: D) extends Arity {

  override def runtimeArity: Int = data.runtimeLength
}

object SizeOf {

  implicit def observe[D <: HList, N <: Nat](
      implicit
      length: hlist.Length.Aux[D, N],
      simplify: NatAsOp[N]
  ): SizeOf[D] =>> LeafArity.Derived[NatAsOp[N]] = {

    ProveArity.from[SizeOf[D]].=>> { v =>
      new LeafArity.Derived(simplify)
    }
  }

  //  implicit def fromNat[N <: Nat](n: N)(
  //      implicit simplify: NatAsOp[N]
  //  ): OfSize[Nothing, NatAsOp[N]] = {
  //    new OfSize(simplify.value)
  //  }

  def getConst[D <: HList, N <: Nat](data: D)(
      implicit
      self: SizeOf[D] ~~> LeafArity.Const[NatAsOp[N]]
  ) = {
    val raw = SizeOf[D](data)
    self.apply(raw).value
  }
}
