package amazonRobots

import akka.actor.ActorRef

/**
 * Created by sacry on 16/06/14.
 */
object Protocol {

  sealed trait Message
  case object Ask extends Message
  case class Position(x:Int, y:Int) extends Message


  // ========================================================
  sealed trait GUI
  // case object UpdatePosition extends GUI
  case object Update extends GUI


}
