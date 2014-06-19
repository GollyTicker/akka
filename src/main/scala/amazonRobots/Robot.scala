package amazonRobots

import amazonRobots.Protocol._
import akka.actor.{ActorRef, Actor}

/**
 * Created by sacry on 17/06/14.
 */
class Robot(val id: Int, val p2: Position, val grid:Grid) extends Actor {

  var actors:List[ActorRef] = Nil

  def shortestPath(p1: Position) = ???

  override def receive: Receive = {
    //case RobotAt(i, x, y) =>
    case ActorList(ls) => actors = ls
  }
}

object Robot {

}
