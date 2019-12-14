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

import httpparser._
import httpparser.model.RequestWithBody
import httpparser.model.RequestWithoutBody
import httpparser.model.Method._
import httpparser.model.HttpVersion._

object Main {
  def main(args: Array[String]): Unit = {

    val s = socket(AF_INET, SOCK_STREAM, 0)

    // set non blocking
    fcntl.fcntl(
      s,
      fcntl.F_SETFL,
      fcntl.fcntl(s, fcntl.F_GETFD, 0) | fcntl.O_NONBLOCK
    )

    val flags = stackalloc[CInt]
    !flags = 1
    setsockopt(
      s,
      SOL_SOCKET,
      SO_REUSEADDR,
      flags.asInstanceOf[Ptr[Byte]],
      sizeof[CInt].toUInt
    )
    setsockopt(
      s,
      IPPROTO_TCP,
      TCP_NODELAY,
      flags.asInstanceOf[Ptr[Byte]],
      sizeof[CInt].toUInt
    )

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
      new Function1[Int, Unit] {
        def apply(res: Int) = {
          val len = stackalloc[socklen_t]
          !len = sizeof[sockaddr_in].toUInt
          val client = accept(s, servaddr.asInstanceOf[Ptr[sockaddr]], len)
          val iovec = malloc(sizeof[iovec]).asInstanceOf[Ptr[iovec]]
          iovec._1 = malloc(4096)
          iovec._2 = 4096L
          loop.ring.poll(
            client,
            new Function1[Int, Unit] {
              def apply(res: Int) = {
                val request = HttpParser.parseRequest(iovec._1, iovec._2)
                println(request)
                request match {
                  case RequestWithoutBody(GET, `1.1`, "/", headers) =>
                    println("Got get request!")
                  case _ =>
                }
                loop.ring.poll(client, this, _.prepReadv(client, iovec, 1, 0))
              }
            },
            _.prepReadv(client, iovec, 1, 0)
          )
          loop.ring.poll(s, this)
        }
      }
    )
  }
}
