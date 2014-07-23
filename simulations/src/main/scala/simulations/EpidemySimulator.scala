package simulations

import math.random

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8
    val prevalenceRate: Double = 0.01
    val sickDelay: Int = 6
    val mayDieDelay: Int = 14
    val deathRate = 0.25
    val immunityDelay: Int = 16
    val recoverDelay: Int = 18
    val moveDelay: Int = 5
    val transmissibilityRate: Double = 0.4
    val airTrafficRate: Double = 0.1

    // to complete: additional parameters of simulation
  }

  import SimConfig._

  val persons: List[Person] =
    for (id <- (1 to population).toList) yield new Person(id)
  //(1 to population).foldLeft(List[Person]()){ (ps, id) => new Person(id) :: ps}
  persons.take((population * prevalenceRate).toInt).foreach(_.infect)
  persons.foreach(_.scheduleNextMove)

  class Person(val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false
    var pos = new Pos(randomBelow(roomRows), randomBelow(roomColumns))
    // demonstrates random number generation
    def row: Int = pos.row
    def col: Int = pos.col

    def recover {
      if (!dead) {
        infected = false
        sick = false
        immune = false
      }
    }

    def infectable = !(immune || infected)

    def infect {
      if (infectable) {
        infected = true
        afterDelay(sickDelay) { sick = true }
        afterDelay(mayDieDelay) { mayDie }
        afterDelay(immunityDelay) { getImmune }
        afterDelay(recoverDelay) { recover }
      }
    }

    def mayDie = withProbability(deathRate) { dead = true }

    def getImmune {
      if (!dead) {
        immune = true
        sick = false
      }
    }

    def scheduleNextMove = if (!dead) afterDelay(randomBelow(moveDelay)) { mayMove }

    def mayMove {
      if (!dead) {
        chooseRoom.foreach { moveInto }
        scheduleNextMove
      }
    }

    def moveInto(p: Pos) {
      pos = p
      withProbability(transmissibilityRate) { infect }
    }

    def chooseRoom: Option[Pos] = {
      movesToValidRooms(pos) match {
        case Nil => None
        case moves => {
          Some(moves(randomBelow(moves.length)).apply(pos))
        }
      }
    }

  }

  case class Pos(private val rowPar: Int, private val colPar: Int) {
    def row = (roomRows + rowPar) % roomRows
    def col = (roomColumns + colPar) % roomColumns

    override def equals(o: Any) = o match {
      case p: Pos => row == p.row && col == p.row
      case _ => false
    }

  }

  def personsInRoom(pos: Pos): List[Person] = persons.filter(p => p.pos == pos)

  def infectionInRoom(pos: Pos): Boolean = personsInRoom(pos).exists { person => person.sick || person.dead }

  type Move = (Pos) => Pos
  val moves: List[Move] = List(
    p => Pos(p.row + 1, p.col),
    p => Pos(p.row, p.col + 1),
    p => Pos(p.row, p.col - 1),
    p => Pos(p.row - 1, p.col))

  def movesToValidRooms(pos: Pos) = {
    withProbability(airTrafficRate)(airTrafficMoves(pos)).getOrElse(moves)
  }.filter(move => !infectionInRoom(move(pos)))

  def airTrafficMoves(pos: Pos): List[Move] = for {
    destRow <- (0 until roomRows).toList
    destCol <- (0 until roomColumns) 
    if (! (destRow == pos.row && destCol == pos.col))
  } yield { p: Pos => Pos(destRow, destCol) }

  def withProbability[A](rate: Double)(action: => A): Option[A] = {
    if (randomBelow(100) <= 100 * rate) Option(action)
    else None
  }

}
