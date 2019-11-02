package uring

import scala.scalanative.runtime.Intrinsics.{castObjectToRawPtr, castRawPtrToObject, castRawPtrToLong, castLongToRawPtr}
import scala.collection.mutable
import scala.scalanative.runtime.RawPtr

object callbacks {
  private val callbacks = mutable.Set.empty[() => Unit]

  @inline def nonEmpty: Boolean = callbacks.nonEmpty

  @inline
  def rawPtrToFunction(rawptr: RawPtr): () => Unit = {
    castRawPtrToObject(rawptr).asInstanceOf[() => Unit]
  }

  @inline
  def functionToRawPtr(f: () => Unit): RawPtr =
    castObjectToRawPtr(f)

  def +=(f: () => Unit): RawPtr = {
    callbacks += f
    functionToRawPtr(f)
  }

  def -=(f: () => Unit): Unit = callbacks -= f

  def -=(data: RawPtr): Unit = {
    val f = rawPtrToFunction(data)
    callbacks -= f
  }

  def apply(data: RawPtr): () => Unit = {
    castRawPtrToObject(data).asInstanceOf[() => Unit]
  }
}
