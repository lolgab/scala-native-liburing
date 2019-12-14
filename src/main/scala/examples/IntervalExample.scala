package examples
import scala.scalajs.js.timers.SetIntervalHandle
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object IntervalExample {
  def main2(args: Array[String]): Unit = {
    var i = 5
    var handle: SetIntervalHandle = null
    handle = scalajs.js.timers.setInterval(1000) {
      println(s"Count = $i")
      i -= 1
      Future(println("Hello from future"))
      if (i < 0) scalajs.js.timers.clearInterval(handle)
    }
  }
}
