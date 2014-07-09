package simulations

object sim {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val a: Simulator#Action = () => {
    println("nothing")
  }                                               //> a  : () => Unit = <function0>
  
  val b = () => a                                 //> b  : () => () => Unit = <function0>
  b()()                                           //> nothing
  
  val c: Simulator#Action = a                     //> c  : () => Unit = <function0>
  
}