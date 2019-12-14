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

  // def readAsync(fd: Int, cb: (Ptr[Byte], Long) => Unit): Unit = {
  //   val f = () => {}
  //   val beforeSubmit: Sqe => Unit = sqe => {
  //     sqe.prepReadv(fd)
  //   }
  //   ring.poll(fd, f)
  // }
  def writeAsync(cb: (Ptr[Byte], Long) => Unit): Unit = ???
}
