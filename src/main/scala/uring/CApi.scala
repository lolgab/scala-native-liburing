package uring

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.socket._
import scala.scalanative.unsafe.Nat._2

@link("uring")
@link("uringhelpers")
@extern
object CApi {
  type __kernel_timespec = CStruct2[Long, Long]
  type io_uring_cqe = CStruct3[Long, Int, Int]
  type itimerspec = CStruct2[__kernel_timespec, __kernel_timespec]
  def socketpair(
      domain: CInt,
      tpe: CInt,
      protocol: CInt,
      socket_vector: Ptr[CArray[Int, _2]]
  ): CInt = extern
  def timerfd_create(clockid: CInt, flags: CInt): CInt = extern
  def timerfd_settime(
      fd: CInt,
      flags: CInt,
      new_value: Ptr[itimerspec],
      old_value: Ptr[itimerspec]
  ): CInt = extern
  def io_uring_queue_init(entries: CInt, ring: Ptr[Byte], flags: CInt): CInt =
    extern
  def io_uring_get_sqe(ring: Ptr[Byte]): Ptr[Byte] = extern
  def io_uring_queue_exit(ring: Ptr[Byte]): Unit = extern
  @name("scalanative_io_uring_cqe_seen")
  def io_uring_cqe_seen(ring: Ptr[Byte], cqe: Ptr[io_uring_cqe]): Unit = extern
  @name("scalanative_io_uring_prep_poll_add")
  def io_uring_prep_poll_add(
      sqe: Ptr[Byte],
      fd: CInt,
      poll_mask: CShort
  ): Unit = extern
  @name("scalanative_io_uring_prep_poll_remove")
  def io_uring_prep_poll_remove(sqe: Ptr[Byte], user_data: Long): Unit =
    extern
  @name("scalanative_io_uring_prep_timeout")
  def io_uring_prep_timeout(
      sqe: Ptr[Byte],
      ts: Ptr[__kernel_timespec],
      count: CInt,
      flags: CInt
  ): Unit = extern
  @name("scalanative_io_uring_prep_timeout_remove")
  def io_uring_prep_timeout_remove(
      sqe: Ptr[Byte],
      user_data: Long,
      count: CInt
  ): Unit = extern
  @name("scalanative_io_uring_prep_writev")
  def io_uring_prep_writev(
      sqe: Ptr[Byte],
      fd: CInt,
      iovecs: Ptr[iovec],
      nr_vecs: CInt,
      offset: CLongInt
  ): Unit = extern
  @name("scalanative_io_uring_prep_readv")
  def io_uring_prep_readv(
      sqe: Ptr[Byte],
      fd: CInt,
      iovecs: Ptr[iovec],
      nr_vecs: CInt,
      offset: CLongInt
  ): Unit = extern
  def io_uring_submit(ring: Ptr[Byte]): CInt = extern
  @name("scalanative_io_uring_wait_cqe")
  def io_uring_wait_cqe(ring: Ptr[Byte], cqe: Ptr[Ptr[io_uring_cqe]]): CInt =
    extern
  @name("scalanative_io_uring_peek_cqe")
  def io_uring_peek_cqe(ring: Ptr[Byte], cqe: Ptr[Ptr[io_uring_cqe]]): CInt =
    extern
  @name("scalanative_io_uring_cqe_get_data")
  def io_uring_cqe_get_data(cqe: Ptr[Byte]): Long = extern
  @name("scalanative_io_uring_sqe_set_data")
  def io_uring_sqe_set_data(sqe: Ptr[Byte], data: Long): Unit = extern
  @name("scalanative_io_uring_prep_accept")
  def io_uring_prep_accept(
      sqe: Ptr[Byte],
      fd: CInt,
      addr: Ptr[sockaddr],
      addr_len: Ptr[socklen_t],
      flags: CInt
  ): Unit = extern
  @name("scalanative_io_uring_size")
  def io_uring_size(): CSize = extern
}
