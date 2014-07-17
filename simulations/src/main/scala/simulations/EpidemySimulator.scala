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
    val deathProbablity = 0.25
    val immunityDelay: Int = 16
    val recoverDelay: Int = 18

    // to complete: additional parameters of simulation
  }

  import SimConfig._

  val persons: List[Person] =
    for (id <- (1 to population).toList) yield new Person(id)
  //(1 to population).foldLeft(List[Person]()){ (ps, id) => new Person(id) :: ps}
  persons.take((population * prevalenceRate).toInt).foreach(_.infect)

  class Person(val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    def recover {
      if (!dead) {
        infected = false
        sick = false
        immune = false
      }
    }

    def infect {
      infected = true
      afterDelay(sickDelay) { sick = true }
      afterDelay(mayDieDelay) { mayDie }
      afterDelay(immunityDelay) { getImmune }
      afterDelay(recoverDelay) { recover }
    }

    def mayDie {
      val deathLimit = 100 * deathProbablity
      if (randomBelow(100) <= deathLimit) dead = true
    }

    def getImmune {
      if (!dead) {
        immune = true
        sick = false
      }
    }
  }

}
