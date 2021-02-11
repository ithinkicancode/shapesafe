package org.shapesafe.core.shape.op

import org.shapesafe.core.Poly1Base
import org.shapesafe.core.arity.binary.AssertEqual
import org.shapesafe.core.arity.{Arity, Leaf}
import org.shapesafe.core.axis.Axis.->>
import shapeless.ops.record.{Keys, Selector}
import shapeless.{::, HList, NotContainsConstraint, Witness}

trait EinSumCondition extends Poly1Base[(HList, (_ <: String) ->> Arity), HList] {

  import org.shapesafe.core.arity.ProveArity._

  implicit def IfExistingName[
      OLD <: HList,
      N <: String,
      D1 <: Arity,
      D2 <: Arity,
      O <: Leaf
  ](
      implicit
      name: Witness.Aux[N],
      selector: Selector.Aux[OLD, N, D1],
      lemma: AssertEqual[D1, D2] ~~> Proof.Aux[O]
  ): ==>[(OLD, N ->> D2), (N ->> O) :: OLD] = {

    buildFrom[(OLD, N ->> D2)].to {

      case (old, field) =>
        import shapeless.record._

        val d1 = old.apply(name)
        val d2 = field

        val d_new: O = lemma.apply(AssertEqual(d1, d2)).out

        d_new.asInstanceOf[N ->> O] :: old
    }

  }

}

object EinSumCondition extends EinSumCondition {

  implicit def IfNewName[
      OLD <: HList,
      N <: String,
      D <: Arity,
      OLDNS <: HList
  ](
      implicit
      name: Witness.Aux[N],
      keys: Keys.Aux[OLD, OLDNS],
      NotContainsConstraint: NotContainsConstraint[OLDNS, N]
  ): ==>[(OLD, N ->> D), (N ->> D) :: OLD] = {

    buildFrom[(OLD, N ->> D)].to {
      case (old, field) =>
        field.asInstanceOf[N ->> D] :: old
    }
  }
}
