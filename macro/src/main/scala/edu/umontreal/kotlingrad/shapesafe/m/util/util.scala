package edu.umontreal.kotlingrad.shapesafe.m

import com.tribbloids.graph.commons.util.ScalaReflection

package object util {

  type TypeTag[T] = ScalaReflection.universe.TypeTag[T]

  type Type = ScalaReflection.universe.Type
}
