package MySample

import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.RoundRobinRouter

/**
 * Created by Swaneet on 18.06.2014.
 */
object FibonacchiRouter {

  def main(args: Array[String]) {
    val system = ActorSystem("mySystem")
    val numActors = 15
    system.actorOf(Props(classOf[Master], numActors)) ! Start
  }

  sealed trait Message

  case class Request(a: Long, b: Long, n:Int) extends Message

  case class Response(a: Long, b: Long, n:Int) extends Message

  case class Start() extends Message

  class Master(nActors:Int) extends Actor {
    val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nActors)), name = "myRouter")

    def receive = {
      case Start => workerRouter ! Request(0L, 1L, 40)
      case Response(_, res, 0) => println("Result: " + res); context.system.shutdown()
      case Response(a, b, n) => workerRouter ! Request(a,b,n)
    }
  }

  class Worker() extends Actor {
    def receive() = {
      case Request(a, b, n) => sender ! Response(b, a + b, n - 1)
    }
  }

}
