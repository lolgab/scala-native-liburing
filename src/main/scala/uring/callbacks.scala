package uring

import scala.scalanative.runtime.Intrinsics.{
  castObjectToRawPtr,
  castRawPtrToObject,
  castRawPtrToLong,
  castLongToRawPtr
}
import scala.collection.mutable

object callbacks {
  private val callbacks = mutable.Set.empty[() => Unit]

  @inline def nonEmpty: Boolean = callbacks.nonEmpty

  @inline
  def longToFunction(long: Long): () => Unit = {
    castRawPtrToObject(castLongToRawPtr(long)).asInstanceOf[() => Unit]
  }

  @inline
  def functionToLong(f: () => Unit): Long =
    castRawPtrToLong(castObjectToRawPtr(f))

  def +=(f: () => Unit): Long = {
    callbacks += f
    functionToLong(f)
  }

  def -=(f: () => Unit): Unit = callbacks -= f

  def -=(data: Long): Unit = {
    val f = longToFunction(data)
    callbacks -= f
  }

  def apply(data: Long): () => Unit = {
    longToFunction(data).asInstanceOf[() => Unit]
  }
}
