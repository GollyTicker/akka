package MySample

import scala.concurrent.{Await, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout

/**
 * Created by sacry on 17/06/14.
 */
object DemoActor extends App {
  val system = ActorSystem("mySystem")
  val numActors = 4
  implicit val timeout = Timeout(5 seconds)

  for (i <- 0 until numActors) yield system.actorOf(Props(classOf[DemoActor], util.Random.nextInt(50), i), s"demo$i")

  def actorByName(id: Int): ActorRef = {
    val Fut = system.actorSelection(system./("demo" + id)).resolveOne()
    Await.result(Fut, 5 seconds)
  }

  //actorByName(0) ! 0

  system.actorOf(Props(classOf[Asker], system)) ! "Fib"
}

class Asker(system: ActorSystem) extends Actor {
  def receive = {
    case "Fib" => DemoActor.actorByName(0) !(0L, 1L, 40)
    case res: Long => println("Final: " + res); system.shutdown()
  }
}

class DemoActor(magicNumber: Int, id: Int) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  import DemoActor._

  def nextActor(id: Int) = {
    val name = self.path.name
    val n = name.charAt(name.length - 1).toInt
    actorByName((n + 1) % numActors)
  }

  def receive = {
    case (a: Long, b: Long, n: Int) => {
      println(id + ": " + b)
      Thread.sleep(50)
      if (n > 0) {
        nextActor(id)
          .ask((b, a + b, n - 1))
          .pipeTo(sender())
      }
      else {
        sender() ! b
      }
    }
    case x: Int if x > 4000 => {
      val s = "Finished with " + x
      println(s)
      sender() ! s
    }
    case x: Int => {
      println(id + ": " + x)
      //println(id + ", " + self.path + ": " + x)
      Thread.sleep(1);
      nextActor(id) ask (x + magicNumber) map {
        x => println(id + ", " + self.path + ": " + x); x
      } pipeTo (sender())
    }
    case _ => {
      println(id + ": unknown")
    }
  }
}