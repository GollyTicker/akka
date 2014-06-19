package amazonRobots

import akka.actor.{Props, ActorSystem}
import scala.util.Random
import amazonRobots.Protocol.Position

/**
 * Created by sacry on 16/06/14.
 */
object Main {

  def main(args: Array[String]) = {

    val siml = new Simulation("1110011,0011001,0011001,1011001,1011001,0000000,0022200", verbose = true)

    siml.run(4000) // runs 4000 millis in the simulation

    // println(siml.grid)

  }
}