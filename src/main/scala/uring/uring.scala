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
      cb: Int => Unit,
      beforeSubmit: Sqe => Unit = _ => ()
  ): Long = {
    val sqe = this.sqe()
    beforeSubmit(sqe)
    // sqe.pollAdd(fd, POLLIN)
    val functionPtr = callbacks += cb
    sqe.setData(functionPtr)
    val res = submit()
    if (res == -1) throw new Exception(s"Failed to submit on fd: $fd")
    functionPtr
  }

  def pollCiclic(
      fd: Int,
      cb: Int => Unit,
      beforeSubmit: Sqe => Unit = _ => ()
  ): Long = {
    val f = new Function1[Int, Unit] {
      def apply(res: Int): Unit = {
        cb(res)
        poll(fd, this, beforeSubmit)
      }
    }
    poll(fd, f, beforeSubmit)
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
    val cqePtr = stackalloc[Ptr[io_uring_cqe]]
    val res = io_uring_wait_cqe(ptr, cqePtr)
    if (res != 0) throw new Exception("Error waiting on cqe")
    new Cqe(!cqePtr)
  }

  def peekCqe(): Cqe = {
    val cqePtr = stackalloc[Ptr[io_uring_cqe]]
    val res = io_uring_peek_cqe(ptr, cqePtr)
    if (res != 0) throw new Exception("Error peeking on cqe")
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

  def prepReadv(fd: Int, iovecs: Ptr[iovec], nr_vecs: Int, offset: Long): Unit =
    io_uring_prep_readv(ptr, fd, iovecs, nr_vecs, offset)

  def prepWritev(
      fd: Int,
      iovecs: Ptr[iovec],
      nr_vecs: Int,
      offset: Long
  ): Unit = io_uring_prep_writev(ptr, fd, iovecs, nr_vecs, offset)

  def prepAccept(
      fd: Int,
      addr: Ptr[sockaddr],
      addrLen: Ptr[socklen_t],
      flags: Int
  ): Unit = io_uring_prep_accept(ptr, fd, addr, addrLen, flags)
}
object Sqe {}
class Cqe(val ptr: Ptr[io_uring_cqe]) extends AnyVal {
  def data: Long = ptr._1
  def res: Int = ptr._2
}
