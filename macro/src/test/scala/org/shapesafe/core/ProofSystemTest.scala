package org.shapesafe.core

import org.shapesafe.BaseSpec

object ProofSystemTest {

  trait Term

  case class Simple(name: String) extends Term

  object Sys extends ProofSystem[Term]

  import Sys._

  case class P0() extends Term

  object P0 {

    implicit def axiom: P0 =>> Simple = from[P0].=>> { p =>
      Simple(p.getClass.toString)
    }
  }

  case class P1[T <: Term, M](child: T, meta: M)

  object P1 {

    implicit def axiom[S <: Simple, M]: P1[S, M] =>> Simple = from[P1[S, M]].=>> { p =>
      Simple(s"${p.getClass.toString} -> ${p.child.name}")
    }

    implicit def theorem[
        T <: Term,
        S <: Simple,
        M,
        O <: Term
    ](
        implicit
        lemma1: T ~~> S,
        lemma2: P1[S, M] --> O
    ): P1[T, M] =>> O = from[P1[T, M]].=>> { p =>
      lemma2.valueOf(
        p.copy(lemma1.valueOf(p.child))
      )
    }
  }

}

class ProofSystemTest extends BaseSpec {

  import ProofSystemTest._

  it("can prove P1") {

    val p1 = P1(P0(), 123)

    Sys.at(p1).summonValue
  }
}
