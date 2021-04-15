package org.shapesafe.core.shape.unary

import org.shapesafe.core.Poly1Base
import org.shapesafe.core.arity.Arity
import org.shapesafe.core.axis.Axis
import org.shapesafe.core.axis.Axis.:<<-
import org.shapesafe.core.debugging.InfoCT.Peek
import org.shapesafe.core.shape.LeafShape.><
import org.shapesafe.core.shape._
import shapeless.ops.hlist.At
import shapeless.ops.record.Selector
import shapeless.{Nat, Witness}

case class GetSubscript[ // last step of einsum, contract, transpose, etc.
    S1 <: Shape,
    I <: Index
](
    s1: S1 with Shape,
    index: I
) extends Conjecture1.^[S1] {

  override type _Peek = Peek.InfixW[S1, " GetSubscript ", I]
}

object GetSubscript {

//  object Direct extends ProveShape.SubScope
  //  import Direct._
  import ProveShape._
  import Factory._

  implicit def simplify[
      S1 <: Shape,
      P1 <: LeafShape,
      I <: Index,
      O <: Axis
  ](
      implicit
      lemma1: |-[S1, P1],
      lemma2: Premise.==>[GetSubscript[P1, I], O]
  ): GetSubscript[S1, I] =>> (LeafShape.Eye >< O) = {

    ProveShape.forAll[GetSubscript[S1, I]].=>> { v =>
      val p1: P1 = lemma1.valueOf(v.s1)
      val vv: GetSubscript[P1, I] = v.copy(s1 = p1)

      Shape appendInner lemma2(vv)
    }
  }

  object Premise extends Poly1Base[GetSubscript[_, _], Axis] {

    implicit def byName[
        P1 <: LeafShape,
        N <: String,
        A <: Arity
    ](
        implicit
        _selector: Selector.Aux[P1#Record, N, A]
    ): GetSubscript[P1, Index.Name[N]] ==> (A :<<- N) = {
      forAll[GetSubscript[P1, Index.Name[N]]].==> { v =>
        val p1: P1 = v.s1

        val arity: A = _selector(p1.record)
        val w: Witness.Aux[N] = v.index.w
        (arity.^ :<<- w)
      }
    }

    implicit def byII[
        P1 <: LeafShape,
        N <: Nat,
        O <: Axis
    ](
        implicit
        _at: At.Aux[P1#Static, N, O]
    ): GetSubscript[P1, Index.I_th[N]] ==> O = {
      forAll[GetSubscript[P1, Index.I_th[N]]].==> { v =>
        val p1 = v.s1

        _at(p1.static)
      }
    }
  }
}
