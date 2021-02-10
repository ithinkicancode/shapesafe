package org.shapesafe.core.arity.binary

import org.shapesafe.core.arity.{Arity, ProveArity}
import org.shapesafe.core.arity.ProveArity.~~>
import singleton.ops.{==, Require}

case class AssertEqual[
    +A1 <: Arity,
    +A2 <: Arity
](
    a1: A1,
    a2: A2
) extends Arity {

  override lazy val number: Int = {
    val v1 = a1.number
    val v2 = a2.number

    require(v1 == v2)
    v1
  }
}

trait AssertEqual_Imp0 {

  implicit def unsafe[
      A1 <: Arity,
      A2 <: Arity,
      O <: ProveArity.Proof
  ](
      implicit
      domain: UnknownDomain[A1, A2, O]
  ): UnknownDomain[A1, A2, O]#ForEqual = {
    domain.ForEqual
  }
}

object AssertEqual extends AssertEqual_Imp0 {

  implicit def invar[
      A1 <: Arity,
      A2 <: Arity,
      S1,
      S2
  ](
      implicit
      bound1: A1 ~~> ProveArity.OfStaticImpl[S1],
      bound2: A2 ~~> ProveArity.OfStaticImpl[S2],
      lemma: Require[S1 == S2]
  ): InvarDomain[A1, A2, S1, S2]#ForEqual = {

    val domain = InvarDomain[A1, A2, S1, S2]()(bound1, bound2)

    domain.ForEqual()
  }
}
