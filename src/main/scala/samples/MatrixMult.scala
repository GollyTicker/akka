package MySample

import MySample.MatrixActors.Master
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import scala.concurrent.{ExecutionContext, duration}
import duration._
import ExecutionContext.Implicits.global
import scala.concurrent._
import myUtils.ExecTiming._
import akka.pattern.ask
import myUtils.OSEnvironment._

/**
 * Created by Swaneet on 18.06.2014.
 */
class MatrixActors(val numActors: Int = 15) {
  // http://doc.akka.io/docs/akka/snapshot/general/configuration.html
  import com.typesafe.config._
  val config = ConfigFactory.parseString("akka.log-dead-letters-during-shutdown=off")

  val system = ActorSystem("mySystem",config)

  val master = system.actorOf(Props(classOf[Master],numActors), "MasterActor")

  def mult(a: IMatrix, b: IMatrix): IMatrix = {
    implicit val timeout = Timeout(5 seconds)
    Await.result(master ? Start(a, b) map (_.asInstanceOf[IMatrix]), 5 seconds)
  }

  def shutdown() = system.shutdown()



}

sealed trait Message

case class Request(i: Int, j: Int, dim: Int, a: IMatrix, b: IMatrix) extends Message

case class Response(i: Int, j: Int, x: Int) extends Message

case class Finished(dim: Int, arr: Array[Int]) extends Message

case class Start(a: IMatrix, b: IMatrix) extends Message

object MatrixActors {
  class Master(numActors:Int) extends Actor {
    val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(numActors)), name = "myRouter")

    var asker: ActorRef = _

    var dim: Int = _
    var res: Array[Int] = _
    val set: (Int, Int) => Array[Int] => Int => Unit = (i, j) => arr => v => {
      arr.update(dim * i + j, v);
      ()
    }

    def receive = {
      case Start(a, b) => {
        asker = sender()
        dim = a.dim
        res = Array.fill(dim * dim)(0)

        for (i <- 0 until dim; j <- 0 until dim) {
          workerRouter ! Request(i, j, dim, a, b)
        }

        self ! Finished(dim, res)
      }
      case Finished(dim, res) => {
        asker ! (new IMatrix(dim, res))
      }
      case Response (i, j, x) => set(i, j)(res)(x)
    }
  }

  class Worker() extends Actor {
    def receive() = {
      case Request(i, j, dim, a, b) => {
        val f: Int => Int = k => a.get(i, k) * b.get(k, j)
        val x:Int = (0 until dim).map(f).sum.toInt  // weird non compilation.
        sender ! Response(i, j, x)
      }
    }
  }
}


object A11 {

  def main(args: Array[String]) {
    A11(300)
  }

  def apply(x: Int = 1000, withSlow: Boolean = true) = {
    val m0 = IMatrix(6, (i, j) => if (i == j) i + 1 else 0)
    val m3 = IMatrix(x, (i, j) => i + j)

    val myActors = new MatrixActors(160)

    // printOSSpecs()

    // Cores : 4
    // OS : Windows 7 6.1 (x86)
    // JVM : 1.7.0_21

    // printResultAndMillis( m0 * m0))
    // printResultAndMillis(myActors.mult(m0, m0))

    var slow1 = "x"
    var slow2 = "x"

    if (withSlow) {
      slow1 = millis(m0 slowMult m0)
      slow2 = millis(m3 slowMult m3)
    }

    val fast1 = millis(m0 * m0)
    val fast2 = millis(m3 * m3)

    val actFast1 = millis(myActors.mult(m0, m0))
    val actFast2 = millis(myActors.mult(m3, m3))



    println("slow vs. futures vs. actors:")
    println(s"\tsmall: $slow1 vs. $fast1 vs. $actFast1")
    println(s"\tlarge: $slow2 vs. $fast2 vs. $actFast2")

    myActors.shutdown()
  }

}


// from: https://github.com/GollyTicker/scalaWP14/blob/master/src/A11.scala

class IMatrix(val dim: Int, private val arr: Array[Int]) {

  def apply(i: Int, j: Int) = if (0 <= i && i < dim && 0 <= j && j < dim) Some(arr(i * dim + j)) else None

  val get: (Int, Int) => Int =
    (i, j) => apply(i, j).get

  // (A*B)(i)(j) == sum {k, 1, n, A(i)(k) * B(k)(j)
  def slowMult(that: IMatrix): IMatrix = {
    val res = Array.fill(dim * dim)(0)
    val set: (Int, Int) => Array[Int] => Int => Unit = (i, j) => arr => v => {
      arr.update(dim * i + j, v);
      ()
    }
    def calcElem(i: Int, j: Int) = set(i, j)(res)((0 until dim).map(k => get(i, k) * get(k, j)).sum)
    for {i <- 0 until dim; j <- 0 until dim; a = calcElem(i, j)} yield ()
    new IMatrix(dim, res)
  }

  def *(that: IMatrix): IMatrix = {
    val res = Array.fill(dim * dim)(0)
    val set: (Int, Int) => Array[Int] => Int => Unit = (i, j) => arr => v => {
      arr.update(dim * i + j, v);
      ()
    }

    def calcElem(i: Int, j: Int) = {
      Future(set(i, j)(res) {
        // Änderung 1
        (0 until dim).map(k => arr(i * dim + k) * that.get(k, j)).sum
      })
    }
    val workers = for {
      i <- 0 until dim
      j <- 0 until dim
    } yield calcElem(i, j)

    workers.foreach(f => Await.ready(f, Duration.Inf)) // Änderung 2
    new IMatrix(dim, res)
  }

  override def toString = {
    val srows = for (r <- arr.sliding(dim, dim)) yield r.mkString("(", ",", ")\n")
    "Matrix(\n" + srows.foldLeft("")((s, r) => s + r) + ")"
  }
}

object IMatrix {
  def apply[N](dim: Int, f: (Int, Int) => Int) = {
    val vec = Array.fill(dim * dim)(0)
    for (i <- 0 until dim; j <- 0 until dim)
      vec(i * dim + j) = f(i, j)
    new IMatrix(dim, vec)
  }
}
