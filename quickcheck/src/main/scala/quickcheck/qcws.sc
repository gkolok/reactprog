package quickcheck

object qcws {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  //val qc = (new QuickCheckHeap with BinomialHeap)
  val qc = (new QuickCheckHeap with Bogus4BinomialHeap)
                                                  //> qc  : quickcheck.QuickCheckHeap with quickcheck.Bogus4BinomialHeap = Prop
  val h3_3 = qc.insert(3, Nil)                    //> h3_3  : quickcheck.qcws.qc.H = List(Node(3,0,List()))
  val h = qc.insert(4, qc.insert(0, h3_3))        //> h  : quickcheck.qcws.qc.H = List(Node(4,0,List()), Node(0,1,List(Node(3,0,Li
                                                  //| st()))))
  
  val s = qc.genHeap.sample.get                   //> s  : quickcheck.qcws.qc.H = List(Node(-245900002,1,List(Node(2147483647,0,Li
                                                  //| st()))), Node(-430999444,2,List(Node(0,1,List(Node(938161703,0,List()))), No
                                                  //| de(1,0,List()))))
  val minList = qc.minElements(s)                 //> minList  : List[quickcheck.qcws.qc.A] = List(-430999444, -430999444, -430999
                                                  //| 444, 0, 0, 938161703)
  
  qc.deleteMin(s)                                 //> res0: quickcheck.qcws.qc.H = List(Node(2147483647,0,List()), Node(-430999444
                                                  //| ,2,List(Node(0,1,List(Node(938161703,0,List()))), Node(1,0,List()))))
  qc.deleteMin(qc.deleteMin(s))                   //> res1: quickcheck.qcws.qc.H = List(Node(-430999444,2,List(Node(0,1,List(Node(
                                                  //| 938161703,0,List()))), Node(1,0,List()))))
 
 
  val (h1,h2) = (qc.genHeap.sample.get, qc.genHeap.sample.get)
                                                  //> h1  : quickcheck.qcws.qc.H = List(Node(0,0,List()), Node(-1761999418,1,List(
                                                  //| Node(310810072,0,List()))))
                                                  //| h2  : quickcheck.qcws.qc.H = List(Node(-480300197,0,List()))
  val melded = qc.meld(h1, h2)                    //> melded  : quickcheck.qcws.qc.H = List(Node(-1761999418,2,List(Node(-48030019
                                                  //| 7,1,List(Node(0,0,List()))), Node(310810072,0,List()))))
  val meldedMin = qc.findMin(melded)              //> meldedMin  : quickcheck.qcws.qc.A = -1761999418
  meldedMin == qc.findMin(h1) || meldedMin == qc.findMin(h2)
                                                  //> res2: Boolean = true
  
}