package amazonRobots

import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import amazonRobots.Protocol._
import akka.actor.{ActorRef, Actor}
import AmazonUtils._

/**
 * Created by sacry on 17/06/14.
 */
class Robot(val initPos: Position, val grid:Grid) extends Actor {
  val name = self.path.name

  private var currPos:Position = initPos
  def pos:Position = Position(currPos.x,currPos.y)

  implicit val timeout = Timeout(5 seconds)

  def shortestPath(p1: Position) = ???

  def receive: Receive = {
    case "find others" => {
      context.actorSelection("/user/*") ! Ask
    }
    case Ask => sender() ! pos
    case p:Position => println(sender()+ " is at " + p)

    case "Hello!" => println(name + " thinks \"fu\"" ); sender() ! name
  }
}


object Robot {

}
