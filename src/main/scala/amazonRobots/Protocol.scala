package amazonRobots

import akka.actor.ActorRef

/**
 * Created by sacry on 16/06/14.
 */
object Protocol {

  sealed trait Message
  case class Position(x: Int, y: Int) extends Message
  case class NextPosition(x:Int, y:Int) extends  Message
  case class Target(x:Int, y:Int) extends  Message
}
