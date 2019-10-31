package uring

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.uio._
import scala.scalanative.posix.sys.socket._ 
import CApi._
import scala.scalanative.posix.pollEvents._

@link("uring")
@link("uringhelpers")
@extern
object CApi {
  type __kernel_timespec = CStruct2[Long, CLongLong]
  def io_uring_queue_init(entries: CInt, ring: Ptr[Byte], flags: CInt): CInt = extern
  def io_uring_get_sqe(ring: Ptr[Byte]): Ptr[Byte] = extern
  def io_uring_queue_exit(ring: Ptr[Byte]): Unit = extern
  @name("scalanative_io_uring_cqe_seen")
  def io_uring_cqe_seen(ring: Ptr[Byte], cqe: Ptr[Byte]): Unit = extern
  @name("scalanative_io_uring_prep_poll_add")
  def io_uring_prep_poll_add(sqe: Ptr[Byte], fd: CInt, poll_mask: CShort): Unit = extern
  @name("scalanative_io_uring_prep_poll_remove")
  def io_uring_prep_poll_remove(sqe: Ptr[Byte], user_data: Ptr[Byte]): Unit = extern
  @name("scalanative_io_uring_prep_timeout")
  def io_uring_prep_timeout(sqe: Ptr[Byte], ts: Ptr[__kernel_timespec], count: CInt, flags: CInt): Unit = extern
  @name("scalanative_io_uring_prep_timeout_remove")
  def io_uring_prep_timeout_remove(sqe: Ptr[Byte], user_data: Ptr[Byte], count: CInt): Unit = extern
  @name("scalanative_io_uring_prep_writev")
  def io_uring_prep_writev(sqe: Ptr[Byte], fd: CInt, iovecs: Ptr[iovec], nr_vecs: CInt, offset: CLongInt): Unit = extern
  @name("scalanative_io_uring_prep_readv")
  def io_uring_prep_readv(sqe: Ptr[Byte], fd: CInt, iovecs: Ptr[iovec], nr_vecs: CInt, offset: CLongInt): Unit = extern
  def io_uring_submit(ring: Ptr[Byte]): CInt = extern
  @name("scalanative_io_uring_wait_cqe")
  def io_uring_wait_cqe(ring: Ptr[Byte], cqe: Ptr[Ptr[Byte]]): CInt = extern
  @name("scalanative_io_uring_peek_cqe")
  def io_uring_peek_cqe(ring: Ptr[Byte], cqe: Ptr[Byte]): CInt = extern
  @name("scalanative_io_uring_cqe_get_data")
  def io_uring_cqe_get_data(cqe: Ptr[Byte]): Long = extern
  @name("scalanative_io_uring_sqe_set_data")
  def io_uring_sqe_set_data(sqe: Ptr[Byte], data: Long): Unit = extern
  @name("scalanative_io_uring_prep_accept")
  def io_uring_prep_accept(sqe: Ptr[Byte], fd: CInt, addr: Ptr[sockaddr], addr_len: Ptr[socklen_t], flags: CInt): Unit = extern
}
class URing private (val ptr: Ptr[Byte]) extends AnyVal {
  def poll(fd: Int, cb: () => Unit): Unit = {
    val sqe = this.sqe()
    sqe.pollAdd(fd, POLLIN)
    val functionPtr = Main.addFunction(cb)
    io_uring_sqe_set_data(sqe.ptr, functionPtr)
    val res = submit()
    if(res == -1) throw new Exception(s"Failed to submit on fd: $fd")
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
    if(res != 0) throw new Exception("Failed to create uring")
    new URing(ptr)
  }
}
class Sqe (val ptr: Ptr[Byte]) extends AnyVal {
  def pollAdd(fd: Int, pollMask: Short) = io_uring_prep_poll_add(ptr, fd, pollMask)
}
object Sqe {
  
}
class Cqe (val ptr: Ptr[Byte]) extends AnyVal {
  def getData(): Long = io_uring_cqe_get_data(ptr)
}
