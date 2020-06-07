package edu.umontreal.kotlingrad.shapesafe

import shapeless.HNil
import shapeless.::

class ConstraintSpec extends BaseSpec {

  describe("prove") {

    it("base") {
      val proof = implicitly[Constraint.ElementOfType[HNil, Int]]

      assert(proof.ttg.tpe.toString == "Int")
    }

    it("1 element") {

      val proof = implicitly[Constraint.ElementOfType[Int :: HNil, Int]]

      assert(proof.ttg.tpe.toString == "Int")
    }

    it("2 elements") {

      val proof = implicitly[Constraint.ElementOfType[Int :: Int :: HNil, Int]]

      assert(proof.ttg.tpe.toString == "Int")
    }

    it("5 elements") {

      val proof = implicitly[Constraint.ElementOfType[Int :: Int :: Int :: Int :: Int :: HNil, Int]]

      assert(proof.ttg.tpe.toString == "Int")
    }

    it("if members are of different subtypes") {

      import java.lang

      val proof =
        implicitly[
          Constraint.ElementOfType[lang.Integer :: lang.Double :: lang.Float :: lang.Long :: HNil, lang.Number]]

      assert(proof.ttg.tpe.toString == "Number")
    }
  }

  describe("CANNOT prove") {
    it("if a member is not a subtype") {

      assertDoesNotCompile(
        """
          |val proof = implicitly[Constraint.ElementOfType[Int :: Int :: Int :: Double :: Int :: HNil, Int]]
          |""".stripMargin
      )
    }
  }

}
