package scala.messaging

import scala.scalanative.posix.unistd.fork
import uring._

class Isolate(body: => Unit) {
  private var _socket: Int = -1

  def socket = _socket

  private val res = fork()
  private val (parentSocket, childSocket) = ForkUtils.createSocketPair()
  if (res == 0) { // I'm the isolate
    _socket = childSocket
    Loop.loop = new Loop()
    body
  } else { // I'm the parent
    _socket = parentSocket
  }
}

object Isolate {
  def apply(body: => Unit): Isolate = new Isolate(body)
}
