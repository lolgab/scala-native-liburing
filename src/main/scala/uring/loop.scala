package uring

import scala.scalanative.unsafe._

object loop {
  private val globalZone = Zone.open()
  scala.scalanative.runtime.ExecutionContext.global.execute(
    new Runnable {
      def run() = loop.run()
    }
  )

  val ring = URing()
  def run(): Unit = {
    scala.scalanative.runtime.loop()
    while (callbacks.nonEmpty) {
      val cqe = ring.waitCqe()
      callbacks(cqe.data)()
      ring.cqeSeen(cqe)
      scala.scalanative.runtime.loop()
    }
  }

  def readAsync(cb: (Ptr[Byte], Long) => Unit): Unit = ???
  def writeAsync(cb: (Ptr[Byte], Long) => Unit): Unit = ???
}
