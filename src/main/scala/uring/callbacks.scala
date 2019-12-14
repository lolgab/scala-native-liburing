package uring

import scala.scalanative.runtime.Intrinsics.{
  castObjectToRawPtr,
  castRawPtrToObject,
  castRawPtrToLong,
  castLongToRawPtr
}
import scala.collection.mutable

object callbacks {
  private val callbacks = mutable.Set.empty[Int => Unit]

  @inline def nonEmpty: Boolean = callbacks.nonEmpty

  @inline
  def longToFunction(long: Long): Int => Unit = {
    castRawPtrToObject(castLongToRawPtr(long)).asInstanceOf[Int => Unit]
  }

  @inline
  def functionToLong(f: Int => Unit): Long =
    castRawPtrToLong(castObjectToRawPtr(f))

  def +=(f: Int => Unit): Long = {
    callbacks += f
    functionToLong(f)
  }

  def -=(f: Int => Unit): Unit = callbacks -= f

  def -=(data: Long): Unit = {
    val f = longToFunction(data)
    callbacks -= f
  }

  def apply(data: Long): Int => Unit = {
    longToFunction(data)
  }
}
