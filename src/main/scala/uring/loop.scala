package uring

import scala.scalanative.unsafe._
object Loop {
  var loop: Loop = new Loop()
  scala.scalanative.runtime.ExecutionContext.global.execute(
    new Runnable {
      def run() = loop.run()
    }
  )
  def callbacks = loop.callbacks
}
class Loop {
  val ring = URing()
  val callbacks = new Callbacks()
  def run(): Unit = {
    scala.scalanative.runtime.loop()
    while (callbacks.nonEmpty) {
      val cqe = ring.waitCqe()
      callbacks(cqe.data)(cqe.res)
      ring.cqeSeen(cqe)
      scala.scalanative.runtime.loop()
    }
  }

  def runOnceNonBlocking(): Unit = {
    scala.scalanative.runtime.loop()
    if (callbacks.nonEmpty) {
      val cqe = ring.peekCqe()
      callbacks(cqe.data)(cqe.res)
      ring.cqeSeen(cqe)
      scala.scalanative.runtime.loop()
    }
  }

  def readAsync(fd: Int, cb: (Ptr[Byte], Long) => Unit): Unit = ???
  def writeAsync(cb: (Ptr[Byte], Long) => Unit): Unit = ???
}
