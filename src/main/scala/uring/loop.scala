package uring

import scala.scalanative.unsafe.Zone

object loop {
  private val globalZone = Zone.open()
  scala.scalanative.runtime.ExecutionContext.global.execute(new Runnable {
    def run() = loop.run()
  })

  val ring = URing()(globalZone)
  def run(): Unit = {
    while (callbacks.nonEmpty || ExecutionContext.hasRunnables) {
      ExecutionContext.runRunnables()
      if(callbacks.nonEmpty) {
        val cqe = ring.waitCqe()
        callbacks(cqe.getData())()
        ring.cqeSeen(cqe)
      }
    }
  }
}
