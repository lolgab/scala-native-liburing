package examples
import scala.messaging.Isolate

object IsolatesExample {
  def main(args: Array[String]): Unit = {
    val s = Isolate {
      println("from isolate")
    }
  }
}
