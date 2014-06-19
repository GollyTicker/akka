package amazonRobots

import akka.actor.{ActorRef, Props, ActorSystem}
import amazonRobots.Protocol.{Update, Position}
import scala.concurrent.ExecutionContext.Implicits.global
import AmazonUtils._

/**
 * Created by Swaneet on 17.06.2014.
 */
class Simulation(
                 val gridString: String,
                 val numRobots: Int = 6,
                 val orderMaxSize: Int = 50,
                 val dlTime: Long = 5000L,
                 val verbose: Boolean = false)  // schade, dass man dieses "verbose" nicht implicit machen kann...
{


  // initialization
  val system = ActorSystem("NaSC")
  val realWorld = Grid(gridString) // changing grid
  val staticGrid = Grid(gridString)
  if (verbose) println(s"Initial grid: $realWorld")

  // insert robots
  val robots:List[ActorRef] =
    (0 until numRobots).map{ i:Int =>
      // create the robots. give them their id, a new position and a map of the grid
      system.actorOf(Props(classOf[Robot], realWorld.newRobPosition, staticGrid))
    }.toList

  robots(0) ! "find others"

  val renderer = system.actorOf(Props(classOf[Renderer], realWorld, robots))


  if (verbose) robots.foreach(println)
  if (verbose) println(s"Occupied Grid: $realWorld")

  // es können die Artikel und Orders erzeugt werden.
  val articles = AmazonUtils.articles(realWorld)

  // val sampleOrder = new Order(List(art1,art2,art3),dlTime)

  def run(ms: Long) = {
    // here Code that runs "ms" milliseconds of the virtual world
    // changing positions of robots, if the 5s of changing is elapsed
    // decrementing remaining time of (un)loading articles.
  }

  import scala.concurrent.duration._
  system.scheduler.schedule(0 milliseconds, 1 seconds, renderer, Update)

}

object Simulation {
  case class Order(articles: List[Article], dlTime:Long) {
    val size = articles.map(_.productSize).sum
    val numProducts = articles.size
    val unloadTime = numProducts * dlTime
    override def toString = "Order("+articles.toString+")"
  }

    //def whereCanIGetYou: List[Position] = grid.accessibleNeighbors(productPos)
}

