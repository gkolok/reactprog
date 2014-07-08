package quickcheck

object qcws {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val b1 = (new QuickCheckHeap with Bogus1BinomialHeap)
                                                  //> b1  : quickcheck.QuickCheckHeap with quickcheck.Bogus1BinomialHeap = Prop
  b1.insert(3, Nil)                               //> res0: quickcheck.qcws.b1.H = List(Node(3,0,List()))
  val b2 = b1.insert(0, b1.insert(3, Nil))        //> b2  : quickcheck.qcws.b1.H = List(Node(0,1,List(Node(3,0,List()))))
  
  b1.findMin(b2)                                  //> res1: quickcheck.qcws.b1.A = 0
  
  b2 match {
    case t::ts => t
  }                                               //> res2: quickcheck.qcws.b1.Node = Node(0,1,List(Node(3,0,List())))
}