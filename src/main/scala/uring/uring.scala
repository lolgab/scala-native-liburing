package uring

import scala.scalanative.unsafe._
import scala.scalanative.libc.stdlib.malloc
import scala.scalanative.posix.sys.uio._
import scala.scalanative.posix.sys.socket._
import CApi._
import scala.scalanative.posix.pollEvents._

class URing private (val ptr: Ptr[Byte]) extends AnyVal { self =>
  def poll(
      fd: Int,
      cb: () => Unit,
      beforeSubmit: Sqe => Unit = _ => ()
  ): Long = {
    val sqe = this.sqe()
    sqe.pollAdd(fd, POLLIN)
    val functionPtr = callbacks += cb
    sqe.setData(functionPtr)
    beforeSubmit(sqe)
    val res = submit()
    if (res == -1) throw new Exception(s"Failed to submit on fd: $fd")
    functionPtr
  }

  def pollCiclic(
      fd: Int,
      cb: () => Unit,
      beforeSubmit: Sqe => Unit = _ => ()
  ): Long = {
    val sqe = this.sqe()
    sqe.pollAdd(fd, POLLIN)
    val f = new Function0[Unit] {
      def apply(): Unit = {
        cb()
        val sqe = self.sqe()
        sqe.pollAdd(fd, POLLIN)
        sqe.setData(callbacks.functionToLong(this))
        beforeSubmit(sqe)
        val res = submit()
        if (res == -1) throw new Exception(s"Failed to submit on fd: $fd")
      }
    }
    val functionPtr: Long = callbacks += f
    sqe.setData(functionPtr)
    beforeSubmit(sqe)
    val res = submit()
    if (res == -1) throw new Exception(s"Failed to submit on fd: $fd")
    callbacks.functionToLong(f)
  }

  def clearPoll(data: Long) = {
    val sqe = this.sqe()
    callbacks -= data
    sqe.pollRemove(data)
    val res = submit()
    if (res == -1) throw new Exception("Failed to remove fd from polling")
  }

  def sqe(): Sqe = new Sqe(io_uring_get_sqe(ptr))

  def waitCqe(): Cqe = {
    val cqePtr = stackalloc[Ptr[Byte]]
    val res = io_uring_wait_cqe(ptr, cqePtr)
    if (res != 0) throw new Exception("Error waiting on cqe")
    new Cqe(!cqePtr)
  }

  def cqeSeen(cqe: Cqe): Unit = io_uring_cqe_seen(ptr, cqe.ptr)

  def submit() = io_uring_submit(ptr)

  def queueExit(): Unit = io_uring_queue_exit(ptr)
}
object URing {
  def apply(entries: Int = 32): URing = {
    val ptr = malloc(io_uring_size())
    val res = io_uring_queue_init(entries, ptr, 0)
    if (res < 0) throw new Exception("Failed to create uring")
    new URing(ptr)
  }
}
class Sqe(val ptr: Ptr[Byte]) extends AnyVal {
  def pollAdd(fd: Int, pollMask: Short) =
    io_uring_prep_poll_add(ptr, fd, pollMask)
  def pollRemove(data: Long) = io_uring_prep_poll_remove(ptr, data)

  def setData(v: Long): Unit = io_uring_sqe_set_data(ptr, v)
}
object Sqe {}
class Cqe(val ptr: Ptr[Byte]) extends AnyVal {
  def data: Long = io_uring_cqe_get_data(ptr)
}
