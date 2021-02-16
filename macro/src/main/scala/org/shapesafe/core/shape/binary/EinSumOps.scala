package org.shapesafe.core.shape.binary

import org.shapesafe.core.shape.{Names, Shape}
import org.shapesafe.core.util.RecordView
import shapeless.HList
import shapeless.ops.hlist.Mapper

case class EinSumOps[I <: EinSumIndexed.Proto](
    children: Seq[Shape]
)(
    implicit
    override val indexed: I
) extends CanEinSum[I] {

  lazy val getField = indexed.staticView.GetField

  def -->[H_OUT <: HList](names: Names)(
      implicit
      mapper: Mapper.Aux[getField.type, names.Keys, H_OUT],
      toShape: Shape.FromRecord.Case[H_OUT]
  ): toShape.Out = {

    val projected = names.keys.map(getField)

    Shape.FromRecord(projected)
  }
}
