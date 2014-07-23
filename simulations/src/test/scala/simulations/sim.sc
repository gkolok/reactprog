package simulations

import scala.runtime.RichInt
import scala.annotation.tailrec

object sim {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val a: Simulator#Action = () => {
    println("nothing")
  }                                               //> a  : () => Unit = <function0>

  val b = () => a                                 //> b  : () => () => Unit = <function0>
  b()()                                           //> nothing

  val l1, l2 = 3 :: 2 :: 1 :: 0 :: Nil            //> l1  : List[Int] = List(3, 2, 1, 0)
                                                  //| l2  : List[Int] = List(3, 2, 1, 0)
  val l = l1 ::: l2                               //> l  : List[Int] = List(3, 2, 1, 0, 3, 2, 1, 0)
  val c = 3                                       //> c  : Int = 3
  l.splitAt((Math.pow(2, c - 1)).toInt)           //> res0: (List[Int], List[Int]) = (List(3, 2, 1, 0),List(3, 2, 1, 0))

  val cc = new CircuitSuite                       //> cc  : simulations.CircuitSuite = simulations.CircuitSuite@2a17b7b6

  cc.genWire.sample                               //> res1: Option[simulations.Wire] = Some(0)
  val Some(ws) = cc.genLimitedWireList.sample     //> ws  : List[simulations.Wire] = List(0)
  cc.toInt(ws)                                    //> res2: Int = 0
  
  val es = new EpidemySimulator                   //> es  : simulations.EpidemySimulator = simulations.EpidemySimulator@3cb1ffe6
  es.moves.map(_.apply(es.Pos(0,0)))              //> res3: List[simulations.sim.es.Pos] = List(Pos(1,0), Pos(0,1), Pos(0,-1), Pos
                                                  //| (-1,0))
  // cc.genControlList.sample
  es.Pos(1,1)==  es.Pos(9,1)                      //> res4: Boolean = true
  es.airTrafficMoves(es.Pos(6,6)).map(_(es.Pos(0,0)))
                                                  //> res5: List[simulations.sim.es.Pos] = List(Pos(0,0), Pos(0,1), Pos(0,2), Pos(
                                                  //| 0,3), Pos(0,4), Pos(0,5), Pos(0,6), Pos(0,7), Pos(1,0), Pos(1,1), Pos(1,2), 
                                                  //| Pos(1,3), Pos(1,4), Pos(1,5), Pos(1,6), Pos(1,7), Pos(2,0), Pos(2,1), Pos(2,
                                                  //| 2), Pos(2,3), Pos(2,4), Pos(2,5), Pos(2,6), Pos(2,7), Pos(3,0), Pos(3,1), Po
                                                  //| s(3,2), Pos(3,3), Pos(3,4), Pos(3,5), Pos(3,6), Pos(3,7), Pos(4,0), Pos(4,1)
                                                  //| , Pos(4,2), Pos(4,3), Pos(4,4), Pos(4,5), Pos(4,6), Pos(4,7), Pos(5,0), Pos(
                                                  //| 5,1), Pos(5,2), Pos(5,3), Pos(5,4), Pos(5,5), Pos(5,6), Pos(5,7), Pos(6,0), 
                                                  //| Pos(6,1), Pos(6,2), Pos(6,3), Pos(6,4), Pos(6,5), Pos(6,7), Pos(7,0), Pos(7,
                                                  //| 1), Pos(7,2), Pos(7,3), Pos(7,4), Pos(7,5), Pos(7,6), Pos(7,7))
}
  
case class MyMon[T](t: T) {
  
}