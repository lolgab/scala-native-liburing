/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalajs.js.timers

import scalanative.unsafe._
import uring.callbacks
import uring._

/**
  *  <span class="badge badge-non-std" style="float: right;">Non-Standard</span>
  *  Raw JavaScript timer methods.
  *
  *  The methods on this object expose the raw JavaScript methods for timers. In
  *  general it is more advisable to use the methods directly defined on
  *  [[timers]] as they are more Scala-like.
  */
object RawTimers {
  final private val CLOCK_MONOTONIC = 1

  /** Schedule `handler` for execution in `interval` milliseconds.
    *
    *  @param handler the function to call after `interval` has passed
    *  @param interval duration in milliseconds to wait
    *  @return A handle that can be used to cancel the timeout by passing it
    *          to [[clearTimeout]].
    */
  def setTimeout(handler: () => Unit, interval: Double): SetTimeoutHandle = {
    val f = new Function1[Int, Unit] {
      def apply(res: Int): Unit = {
        handler()
        callbacks -= this
      }
    }

    val itimerspec = stackalloc[CApi.itimerspec]
    itimerspec._1._1 = 0
    itimerspec._1._2 = 0
    itimerspec._2._1 = interval.toLong / 1000
    itimerspec._2._2 = (interval.toLong % 1000) * 1000000

    val fd = CApi.timerfd_create(CLOCK_MONOTONIC, 0)
    if (fd < 0) throw new Exception("Failed to create timerfd")
    CApi.timerfd_settime(fd, 0, itimerspec, null)
    loop.ring.poll(fd, f) // TODO Avoid adding function again
    callbacks.functionToLong(f)
  }

  /** Cancel a timeout execution
    *  @param handle The handle returned by [[setTimeout]]
    */
  def clearTimeout(handle: SetTimeoutHandle): Unit = {
    loop.ring.clearPoll(handle)
  }

  /** Schedule `handler` for repeated execution every `interval`
    *  milliseconds.
    *
    *  @param handler the function to call after each `interval`
    *  @param interval duration in milliseconds between executions
    *  @return A handle that can be used to cancel the interval by passing it
    *          to [[clearInterval]].
    */
  def setInterval(handler: () => Unit, interval: Double): SetIntervalHandle = {
    val itimerspec = stackalloc[CApi.itimerspec]
    val secs = interval.toLong / 1000
    val nsecs = (interval.toLong % 1000) * 1000000

    itimerspec._1._1 = secs
    itimerspec._1._2 = nsecs
    itimerspec._2._1 = secs
    itimerspec._2._2 = nsecs

    val fd = CApi.timerfd_create(CLOCK_MONOTONIC, 0)
    if (fd < 0) throw new Exception("Failed to create timerfd")
    CApi.timerfd_settime(fd, 0, itimerspec, null)
    val f = (res: Int) => {
      val buf = stackalloc[Long]
      scalanative.posix.unistd.read(fd, buf, 8)
      handler()
    }
    val data = loop.ring.pollCiclic(fd, f)
    new SetIntervalHandle(data, fd)
  }

  /** Cancel an interval execution
    *  @param handle The handle returned by [[setInterval]]
    */
  def clearInterval(handle: SetIntervalHandle): Unit = {
    val itimerspec = stackalloc[CApi.itimerspec]
    itimerspec._1._1 = 0
    itimerspec._1._2 = 0
    itimerspec._2._1 = 0
    itimerspec._2._2 = 0
    CApi.timerfd_settime(handle.fd, 0, itimerspec, null)

    loop.ring.clearPoll(handle.data)
  }
}
