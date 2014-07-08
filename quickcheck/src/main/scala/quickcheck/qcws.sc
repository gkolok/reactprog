package quickcheck

object qcws {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val qc = (new QuickCheckHeap with BinomialHeap) //> qc  : quickcheck.QuickCheckHeap with quickcheck.BinomialHeap = Prop
//  val b3 = (new QuickCheckHeap with Bogus3BinomialHeap)
  val h3_3 = qc.insert(3, Nil)                    //> h3_3  : quickcheck.qcws.qc.H = List(Node(3,0,List()))
  val h = qc.insert(4, qc.insert(0, h3_3))        //> h  : quickcheck.qcws.qc.H = List(Node(4,0,List()), Node(0,1,List(Node(3,0,Li
                                                  //| st()))))
  
  val s = qc.genHeap.sample                       //> s  : Option[quickcheck.qcws.qc.H] = Some(List(Node(1925878216,0,List())))
  val minList = qc.minElements(s.getOrElse(Nil))  //> minList  : List[quickcheck.qcws.qc.A] = List(1925878216)
  minList.sorted == minList                       //> res0: Boolean = true
  
}