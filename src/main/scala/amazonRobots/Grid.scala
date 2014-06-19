package amazonRobots

import amazonRobots.Protocol.Position
import AmazonUtils._

import scala.util.Random

/**
 * Created by sacry on 17/06/14.
 */
trait Block {
  def p: Position

  def x: Int = p.x

  def y: Int = p.y
}

sealed trait Accessible

case class EmptyBlock(p: Position) extends Block with Accessible {
  override def toString = "0"
}

case class WareBlock(p: Position, article: Option[Article]) extends Block {
  override def toString = "1"
}

case class PackBlock(p: Position) extends Block {
  override def toString = "2"
}

case class OccupiedBlock(p: Position) extends Block {
  override def toString = "3"
}

case class Grid(val positions: String) extends AbstractGrid with BlockOperations with GridConverter { self =>

  val grid: TGrid = fromStringToGrid(positions)

  override def toString = ("-" * 40) + "\n" + grid
    .map(_.map(_.toString).mkString(" ")
    ).mkString("\n")

  def accessiblePositions(): List[Position] = {
    val openGrid: Array[Block] = grid.flatMap(_.filter(gridElem => isAccessible(gridElem.p)))
    openGrid.map(gridElem => gridElem.p).toList
  }

  def neighbors(p:Position):List[Position] = {
    def inBounds(t:Int) = 0 <= t && t < grid.size
    List((p.x+1, p.y+1),(p.x+1, p.y-1),(p.x-1, p.y+1),(p.x-1, p.y-1))
      .filter( t => inBounds(t._1) && inBounds(t._2) )
      .map(t => Position(t._1,t._2))
  }

  // for Article method
  def accessibleNeighbors(p:Position): List[Position] = {
    self.neighbors(p).filter(isTraversable(_))
  }

  def newRobPosition: Position = {
    val pos = Random.shuffle(self.accessiblePositions()).head
    self.occupyPosition(pos)
    pos
  }
}


trait AbstractGrid {
  type TGrid = Array[Array[Block]]
  val grid: TGrid
}

trait BlockOperations extends AbstractGrid {

  def blockAt(p:Position):Block = grid(p.x)(p.y)

  def isAccessible(p: Position): Boolean = {
    blockAt(p).isInstanceOf[Accessible]
  }

  def isOccupied(p: Position): Boolean = {
    blockAt(p).isInstanceOf[OccupiedBlock]
  }

  def isBlocked(p: Position): Boolean = {
    !blockAt(p).isInstanceOf[Accessible]
  }

  // is traversable heißt, dass der Block kein Lagerort
  // und auch keine Packstation ist. Der Block ist derzeit entweder frei,
  // oder von einem anderen Roboter besetzt.
  // (Vielleicht sollten wir die Benennugn von Traversable und Accessible vertauschen.)
  // Derzeit heißt accessible, dass man diesen Grid auch tatsächlich befahren könnte.
  def isTraversable(p:Position) = {
    isAccessible(p) || isOccupied(p)
  }

  def occupyPosition(p: Position): Boolean = {
    val accessible = isAccessible(p)
    if (accessible) {
      grid(p.x).update(p.y, OccupiedBlock(p))
    }
    accessible
  }

  def leavePosition(p: Position): Boolean = {
    val occupied = isOccupied(p)
    if (occupied) {
      grid(p.x).update(p.y, EmptyBlock(p))
    }
    occupied
  }

  def move(source: Position, target: Position): Boolean = {
    val accessible = occupyPosition(target)
    if (accessible)
      leavePosition(source)
    else
      accessible
  }
}

trait GridConverter extends AbstractGrid {
  def fromStringToGrid(s: String): TGrid = {
    val rows: Array[String] = s.split(",")
    def fromCharToGridElem(i: Int, j: Int, c: Char): Block = c match {
      case '0' => EmptyBlock(Position(i, j))
      case '1' => WareBlock(Position(i, j), None)
      case '2' => PackBlock(Position(i, j))
      case '3' => OccupiedBlock(Position(i, j))
    }
    rows.zipWithIndex.map {
      case (row, i) => row.zipWithIndex
        .map {
        case (character, j) => fromCharToGridElem(i, j, character)
      }.toArray
    }
  }

  def fromGridToString(): String = {
    def fromGridElemToChar(g: Block): Char = g match {
      case e: EmptyBlock => '0'
      case w: WareBlock => '1'
      case p: PackBlock => '2'
      case o: OccupiedBlock => '3'
    }
    grid.map {
      case gridElemArray =>
        gridElemArray.map {
          case gridElem => fromGridElemToChar(gridElem)
        }.mkString("")
    }.mkString(",")
  }

}