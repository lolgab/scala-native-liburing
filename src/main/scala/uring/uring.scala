package uring

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.uio._
import scala.scalanative.posix.sys.socket._ 
import CApi._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.runtime.RawPtr

class URing private (val ptr: Ptr[Byte]) extends AnyVal { self =>
  def poll(fd: Int, cb: () => Unit): RawPtr = {
    val sqe = this.sqe()
    sqe.pollAdd(fd, POLLIN)
    val functionPtr = callbacks += cb
    io_uring_sqe_set_data(sqe.ptr, functionPtr)
    val res = submit()
    if(res == -1) throw new Exception(s"Failed to submit on fd: $fd")
    functionPtr
  }

  def pollCiclic(fd: Int, cb: () => Unit): RawPtr = {
    val sqe = this.sqe()
    sqe.pollAdd(fd, POLLIN)
    val f = new Function0[Unit] {
      def apply(): Unit = {
        cb()
        val buf = stackalloc[Long]
        val sqe = self.sqe()
        sqe.pollAdd(fd, POLLIN)
        io_uring_sqe_set_data(sqe.ptr, callbacks.functionToRawPtr(this))
        val res = submit()
        if(res == -1) throw new Exception(s"Failed to submit on fd: $fd")
      }
    }
    val functionPtr = callbacks += f
    io_uring_sqe_set_data(sqe.ptr, functionPtr)
    val res = submit()
    if(res == -1) throw new Exception(s"Failed to submit on fd: $fd")
    callbacks.functionToRawPtr(f)
  }

  def clearPoll(data: RawPtr) = {
    val sqe = this.sqe()
    callbacks -= data
    sqe.pollRemove(data)
    val res = submit()
    if(res == -1) throw new Exception("Failed to remove fd from polling")
  }

  def sqe(): Sqe = new Sqe(io_uring_get_sqe(ptr))

  def waitCqe(): Cqe = {
    val cqePtr = stackalloc[Ptr[Byte]]
    val res = io_uring_wait_cqe(ptr, cqePtr)
    if(res != 0) throw new Exception("Error waiting on cqe")
    new Cqe(!cqePtr)
  }

  def cqeSeen(cqe: Cqe): Unit = io_uring_cqe_seen(ptr, cqe.ptr)

  def submit() = io_uring_submit(ptr)

  def queueExit(): Unit = io_uring_queue_exit(ptr)
}
object URing {
  def apply(entries: Int = 32)(implicit z: Zone): URing = {
    val ptr = z.alloc(1600)
    val res = io_uring_queue_init(entries, ptr, 0)
    if(res < 0) throw new Exception("Failed to create uring")
    new URing(ptr)
  }
}
class Sqe (val ptr: Ptr[Byte]) extends AnyVal {
  def pollAdd(fd: Int, pollMask: Short) = io_uring_prep_poll_add(ptr, fd, pollMask)
  def pollRemove(data: RawPtr) = io_uring_prep_poll_remove(ptr, data)
}
object Sqe {
  
}
class Cqe (val ptr: Ptr[Byte]) extends AnyVal {
  def getData(): RawPtr = io_uring_cqe_get_data(ptr)
}
