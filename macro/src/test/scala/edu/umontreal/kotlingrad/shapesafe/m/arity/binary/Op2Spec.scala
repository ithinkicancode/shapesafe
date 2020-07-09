package edu.umontreal.kotlingrad.shapesafe.m.arity.binary

import edu.umontreal.kotlingrad.shapesafe.m.arity.{Arity, AritySpecFixture, Operand}

class Op2Spec extends AritySpecFixture {

  import edu.umontreal.kotlingrad.shapesafe.m.arity.DSL._

  describe("can prove") {

    it("arity trivially") {

      val p = a.asProof
      p.out.internal.requireEqual(3)
    }

    it("a + b") {

      val op = a + b

      val p = op.asProof
      p.out.internal.requireEqual(7)
    }

    it("a + b + c") {

      val op0 = a + b
      val op = op0 + c

      val p = op.asProof
      p.out.internal.requireEqual(12)
    }

    it("... in 1 line") {

      val op = a + b + c

      val p = op.asProof
      p.out.internal.requireEqual(12)
    }

    it("a + b + c + d") {

      val op = a + b + c + Arity._1.value

      val p = op.asProof
      p.out.internal.requireEqual(13)
    }

    it("b / a") {

      val op = b / a

      val p = op.asProof
      p.out.internal.requireEqual(1)
    }

    it("... NOT if b == 0") {

      val op = a / Arity._0.value

      shouldNotCompile {
        "val p = op.asProof"
      }
    }

    it("(a + b - c) / d") {

      val op = (a + b - c) / Arity._1.value

      val p = op.asProof
      p.out.internal.requireEqual(2)
    }
  }

  describe("CANNOT prove") {

    describe("<Operand Without Proof> +") {

      it("a") {

        val op = Operand.Unprovable + a

        shouldNotCompile(
          "op.asProof"
        )
      }

      it("a + b") {

        val op = Operand.Unprovable + (a + b)

        shouldNotCompile(
          "op.asProof"
        )
      }
    }
  }
}
