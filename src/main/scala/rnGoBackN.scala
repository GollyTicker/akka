/**
 * Created by Swaneet on 04.07.2014.
 */

import akka._
import akka.actor._
import rnGoBackN._

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Random

object rnGoBackN {

  sealed trait Msg

  case class Message(seqNum: Int, contents: String) extends Msg

  case object CorruptedMessage extends Msg

  case class ACK(seqNum: Int) extends Msg

  case class SenderIs(a: ActorRef)

  case class ReceiverIs(a: ActorRef)

  def main(args: Array[String]) {
    val sys = ActorSystem("rnGoBackN")
    val internet: ActorRef = sys.actorOf(Props[Internet])
    val sdr = sys.actorOf(Props(classOf[Sender], (0 to 14).map(_.toString).toList, internet))
    val rcv = sys.actorOf(Props[Receiver])

    internet ! SenderIs(sdr)
    internet ! ReceiverIs(rcv)
  }
}

class Sender(cs: List[String], internet: ActorRef) extends Actor {

  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  val n = 4
  val timeoutNS = 500 millis

  var nxtseq = 0
  var base = 0

  val packets: Map[Int, Message] = (0 to 100).zip(cs).map { t: (Int, String) => (t._1, Message(t._1, t._2))}.toMap[Int, Message]

  var timer: Cancellable = context.system.scheduler.scheduleOnce(100 seconds, self, "blubb")
  timer.cancel()

  val running = context.system.scheduler.schedule(100 millis, 1000 millis, self, "send")

  println(packets)

  def receive = {
    case "send" if nxtseq < packets.size => {
      if (nxtseq < base + n) {
        println("Sender:" + nxtseq)
        internet ! packets(nxtseq)
        if (base == nxtseq) {
          start_timer()
        }
        nxtseq += 1
      }
    }
    case "timeout" => {
      start_timer()
      println("Sender resending " + base + " to " + (nxtseq - 1))
      (base to (nxtseq - 1)) foreach (internet ! packets(_))
    }
    case ACK(ackedSeq) => {
      println("ACKed: " + ackedSeq)
      base = ackedSeq + 1
      if (base == nxtseq) timer.cancel() else start_timer()
      if (base == cs.length) {
        println("FIN")
        running.cancel()
        context.system.shutdown()
      }
    }
    case CorruptedMessage => {
      println("Sender: Discard corrupted ACK")
    }
  }

  def start_timer() = {
    timer.cancel()
    timer = context.system.scheduler.scheduleOnce(timeoutNS, self, "timeout")
  }
}

class Internet() extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  var rnSender: ActorRef = self
  var rnReceiver: ActorRef = self

  val pktloss = 0.15
  //0.25
  val corruptRate = 0.15
  //0.25
  val transmissiondelay = 90L
  //30L

  def dropPkt(b: => Unit) = {
    if (Random.nextDouble() < pktloss) println("Internet Dropped") else b
  }

  def delay(b: => Unit) = {
    Future {
      Thread.sleep(transmissiondelay);
      b
    }
  }

  def corrupt(m: Msg) = if (Random.nextDouble() < corruptRate) CorruptedMessage else m

  def receive = {
    case SenderIs(s) => rnSender = s
    case ReceiverIs(s) => rnReceiver = s
    case m: Msg => {
      if (sender() == rnSender) {
        delay(dropPkt(rnReceiver ! corrupt(m)))
      }
      else {
        delay(dropPkt(rnSender ! corrupt(m)))
      }
    }
  }
}


class Receiver() extends Actor {
  var expseq = 0
  var packet = ACK(expseq - 1)

  def pkt() = packet.copy()

  var receivedPackets: Array[String] = Array()

  def receive = {
    case m@Message(seq, contents) if seq == expseq => {
      receivedPackets = receivedPackets ++ Array(contents)
      println("Receiver: " + receivedPackets.mkString("[", ",", "]"))
      packet = ACK(expseq)
      sender ! pkt()
      expseq += 1
    }
    case m: Msg => {
      println("Unexpected Msg: " + m)
      sender() ! pkt()
    }
  }
}

