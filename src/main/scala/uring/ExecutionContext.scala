package uring

import scala.concurrent._
import scala.collection.mutable
import uring._
import scalanative.unsafe.Zone
import scala.scalanative.posix.netinet.in

object ExecutionContext {
  private val taskQueue = mutable.ListBuffer[Runnable]()

  @inline def hasRunnables: Boolean = taskQueue.nonEmpty
  
  @inline def runRunnables() =
    while (taskQueue.nonEmpty) {
      val runnable = taskQueue.remove(0)
      try runnable.run()
      catch {
        case t: Throwable => global.reportFailure(t)
      }
    }
  val global: ExecutionContextExecutor = new ExecutionContextExecutor {
    @inline def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    @inline def execute(runnable: Runnable): Unit = taskQueue += runnable
  }
}