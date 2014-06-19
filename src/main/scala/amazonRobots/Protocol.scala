package amazonRobots

import akka.actor.ActorRef

/**
 * Created by sacry on 16/06/14.
 */
object Protocol {

  // Es wird angenommen, dass die Actoren zu ihrer
  // Initialisierung eine ID (0 bis (numRobots-1)) erhalten.
  // Außerdem erhalten die eine Liste mit allen ActorRefs (inkl. sich selber).
  // Damit können sie mit { list(id) ! message } eine NAchricht an den Actor mit der ID id verschicken.

  // Messages
  sealed trait Message

  // teile den anderen mit, was ich gerade mache.(Wobei ich der Actor mit der id $id bin.)
  case class IamDoing(id: Int, act:Action) extends Message with Position {
    def x = act.x
    def y = act.y
  }
  // Teile anderen mit wo ich letzendlich hin will.
  case class IwantToReach(id:Int, x:Int, y:Int) extends  Message with Position
  // Actor A(asker) will von Actor B(receiver) Informationen erhalten.
  // Actor B soll dann die IamDoing und IwantToReach Nachricht verschicken.
  case class ExplicitAsk(asker:Int, receiver:Int) extends Message
  // Das "globale" System kann den Actors nach dem Erzeugen eine Liste der ActorRefs schicken. (Damit sie sie speicheren können)
  // Das geht nicht direkt im Konstruktor der Aktoren, da der ActorRef nicht vor der Inkarnation existiert.
  case class ActorList(ls:List[ActorRef]) extends Message


  // Actions (States). Hilfsstruktur um die current Action eines Robots zu kodieren.
  sealed trait Action extends Position
  // ich mache gerade Nichts.
  case class Idle(x:Int, y:Int) extends Action
  // ich bewege mich gerade von (x,y) in die Richtung dir
  case class Moving(x:Int, y:Int, dir:Char, ttd:Long) extends Action
  // Wenn ich gerade am Be-/Entladen bin.
  case class Loading(x:Int, y:Int, ttf:Long) extends Action

  // dir: dir ist 'N', 'S', 'W' oder 'E' für North, South, West oder East.
  //      zu (1,1) heißt N => (1,0) und W => (0,1)
  // ttf: TimeToFinish bezeichnet die Anzahl der virtuellen Millisekunden die der Aktor noch in diesem Zustand bleiben wird

  // Messages die eine Position abgefragt werden können implementieren dieses trait
  trait Position{
    def x: Int
    def y: Int
  }
  object Position{
    def apply(x1:Int,y1:Int):Position = new Position {def x=x1; def y=y1}
  }
}
