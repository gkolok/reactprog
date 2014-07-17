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
  val Some(ws) = cc.genLimitedWireList.sample     //> ws  : List[simulations.Wire] = List(1, 1, 0)
  cc.toInt(ws)                                    //> res2: Int = 3
  // cc.genControlList.sample
}

case class MyMon[T](t: T) {
  
}