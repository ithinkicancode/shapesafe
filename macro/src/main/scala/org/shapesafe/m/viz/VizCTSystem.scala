package org.shapesafe.m.viz

import com.tribbloids.graph.commons.util.reflect.format.TypeFormat
import com.tribbloids.graph.commons.util.viz.TypeVizFormat
import com.tribbloids.graph.commons.util.{HasOuter, TreeFormat}
import org.shapesafe.m.MWithReflection
import shapeless.Witness
import singleton.ops.{+, RequireMsg, RequireMsgSym}

import scala.reflect.macros.whitebox

trait VizCTSystem extends Product {

  import VizCTSystem._

  def vizFormat: TypeVizFormat

  def useTree: Boolean

  final def typeFormat: TypeFormat = vizFormat.base
  final def treeFormat: TreeFormat = vizFormat.treeFormat

  trait InfoOf[T] {
    type Out
  }
  object InfoOf {

    type Aux[T, O <: String] = InfoOf[T] { type Out = O }
    type Lt[T, O <: String] = InfoOf[T] { type Out <: O }

    class ##[T, O] extends InfoOf[T] {
      final type Out = O
    }
  }
  def createInfoOf[T, O] = new InfoOf.##[T, O]

  def apply[I]: Instance[I] = Instance[I]()

  def infer[T](v: T): Instance[T] = apply[T]

  def narrow[T](v: T): Instance[v.type] = apply[v.type]

  case class Instance[I]() {

    def summon[O <: String](
        implicit
        ev: InfoOf.Aux[I, O]
    ): ev.type = ev

    def peek[O <: String](
        implicit
        ev: InfoOf.Aux[I, O],
        emit: EmitInfo["\n" + O]
    ): Unit = {}

    def interrupt[O <: String](
        implicit
        ev: InfoOf.Aux[I, O],
        emit: EmitError["\n" + O]
    ): Unit = {}

    // TODO: impl Should_=:= at compile-time

    //    def shouldBe[B]: Unit = macro Macros.shouldBe[A, B]

    //    def shouldBe[A: WeakTypeTag, B: WeakTypeTag]: Tree = {
    //
    //      val aa: Type = weakTypeOf[A]
    //      val bb: Type = weakTypeOf[B]
    //
    //      if (!(aa =:= bb)) {
    //        val Seq(s1, s2) = Seq(aa, bb).map { v =>
    //          Option(viz.of(v).typeTree.treeString)
    //        }
    //
    //        val diff = StringDiff(s1, s2, Seq(this.getClass))
    //
    //        throw new AssertionError(diff.errorStr)
    //      }
    //
    //      q"Unit"
    //    }
  }

  trait Updated extends VizCTSystem with HasOuter {

    override def outer: VizCTSystem = VizCTSystem.this

    override def vizFormat: TypeVizFormat = VizCTSystem.this.vizFormat
    override def useTree: Boolean = VizCTSystem.this.useTree
  }
}

object VizCTSystem {

  val FALSE = Witness(false)

  type EmitError[T] = RequireMsg[FALSE.T, T]
  type EmitWarning[T] = RequireMsgSym[FALSE.T, T, singleton.ops.Warn]

  type EmitInfo[T] = EmitWarning[T] // should change after the patch

  trait MBase extends MWithReflection {

    def outer: VizCTSystem.type = VizCTSystem.this

    override val c: whitebox.Context

    import u._

    def infoOf[
        T: c.WeakTypeTag,
        SELF <: VizCTSystem: c.WeakTypeTag
    ]: c.Tree = {

      val tt: Type = weakTypeOf[T]

      val self = {
        val tt = weakTypeOf[SELF].dealias

        val r = refl.TypeView(tt).getOnlyInstance
        r.asInstanceOf[VizCTSystem]
      }
      val useTree = self.useTree

      val str = if (useTree) {

        viz.formattedBy(self.vizFormat).of(tt).treeString
      } else {

        refl.TypeView(tt).formattedBy(self.typeFormat).text
      }

      val name: String = self.getClass.getCanonicalName.stripSuffix("$")
      val liftSelf = c.parse(name)

      q"$liftSelf.createInfoOf[$tt, $str]"
    }

    //    def T[A: WeakTypeTag]: Tree = {
    //
    //      val tt: Type = weakTypeOf[A]
    //      val str = refl.TypeView(tt).Display(format).base
    //
    //      val w = Witness(str)
    //      val sTT = w.T
    // TODO: figure out how it works in shapeless?

    //      new SingletonTypeMacros(c).fieldTypeCarrier(sTT)
    //    }

    //    def report[A: WeakTypeTag](fn: String => Unit): Tree = {
    //
    //      val aa: Type = weakTypeOf[A]
    //      val str = viz.of(aa).typeTree.treeString
    //
    //      fn(str)
    ////      c.abort(c.enclosingPosition, str)
    //
    //      q"Unit"
    //    }

  }

  final class Macros(val c: whitebox.Context) extends MBase
}
