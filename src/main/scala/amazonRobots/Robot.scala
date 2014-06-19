package amazonRobots

import amazonRobots.Protocol.Position
import akka.actor.Actor

/**
 * Created by sacry on 17/06/14.
 */
class Robot(name: String, p2: Position) extends Actor {

  def shortestPath(p1: Position) = ???

  override def receive: Receive = {
    case Position(x, y) =>
      ???
  }
}

object Robot {

}
