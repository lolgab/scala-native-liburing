package uring

import scala.scalanative.posix.sys.socket._
import scala.scalanative.unsafe._
import scala.scalanative.unsafe.Nat._2

object ForkUtils {
  def createSocketPair(): (Int, Int) = {
    val sockets = stackalloc[CArray[Int, _2]]
    val res = CApi.socketpair(AF_UNIX, SOCK_STREAM, 0, sockets)
    if (res != 0) throw new Exception("Failed to create socketpair.")
    val arr = !sockets
    (arr(0), arr(1))
  }
}
