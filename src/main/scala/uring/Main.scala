package uring

import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.netinet.in._
import scala.scalanative.posix.netinet.tcp._
import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.uio._
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.unsigned._
import CApi._
import scala.scalanative.posix.arpa.inet._
import scala.collection.mutable
import scala.scalanative.posix.sys.uio.iovec
import scala.scalanative.libc.stdlib.malloc
import scala.scalanative.posix.fcntl

object Main {
  def main2(args: Array[String]): Unit = {

    val s = socket(AF_INET, SOCK_STREAM, 0)

    // set non blocking
    fcntl.fcntl(s, fcntl.F_SETFL, fcntl.fcntl(s, fcntl.F_GETFD,0) | fcntl.O_NONBLOCK)

    val flags = stackalloc[CInt]
    !flags = 1
    setsockopt(s, SOL_SOCKET, SO_REUSEADDR, flags.asInstanceOf[Ptr[Byte]], sizeof[CInt].toUInt)
    setsockopt(s, IPPROTO_TCP, TCP_NODELAY, flags.asInstanceOf[Ptr[Byte]], sizeof[CInt].toUInt)
    
    val servaddr = stackalloc[sockaddr_in]
    servaddr.sin_family = AF_INET.toUShort
    inet_pton(
      AF_INET,
      c"127.0.0.1",
      servaddr.sin_addr.toPtr.asInstanceOf[Ptr[Byte]]
    )
    servaddr.sin_port = htons(8000.toUShort)
    bind(s, servaddr.asInstanceOf[Ptr[sockaddr]], sizeof[sockaddr_in].toUInt)
    listen(s, 128)
    loop.ring.poll(
      s,
      () => {
        val len = stackalloc[socklen_t]
        !len = sizeof[sockaddr_in].toUInt
        val time = System.nanoTime()
        val client = accept(s, servaddr.asInstanceOf[Ptr[sockaddr]], len)
        println(System.nanoTime() - time)
        loop.ring.poll(client, () => {
          val iovec = malloc(sizeof[iovec]).asInstanceOf[Ptr[iovec]]
          iovec._1 = malloc(32)
          iovec._2 = 32L
          readv(client, iovec, 1)
          iovec._1(31) = 0
          Zone { implicit z =>
            println(fromCString(iovec._1))
          }
        })
      }
    )
  }
}
