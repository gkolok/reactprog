package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a 
  }

  property("min2") = forAll { (a: Int, b: Int) =>
    val h = insert(b, insert(a, empty))
    findMin(h) == Math.min(a, b)
  }

  property("deleteMin1") = forAll { a: Int =>
    val h = insert(a, empty)
    isEmpty(deleteMin(h))
  }
  
  property("findMin, deleteMin -> ordered elements") = forAll { l: List[Int] =>
    l.sorted == minElements(genHeapFromList(l))
  }
  
  property("findMin of melded heaps") = forAll { (h1: H, h2: H) =>
    val melded = meld(h1, h2)
    val meldedMin = findMin(melded)
    meldedMin == findMin(h1) || meldedMin == findMin(h2) 
  }
  
  def genHeapFromList(l: List[Int]): H = l match {
    case Nil => empty
    case t::ts => insert(t, genHeapFromList(ts))
  }
    
  
  def minElements(h: H): List[A] =
    if (isEmpty(h)) Nil
    else findMin(h)::minElements(deleteMin(h)) 
  
  lazy val genHeap: Gen[H] = for {
    k <- arbitrary[Int]
    h <- oneOf(empty, genHeap)
  } yield insert(k, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)
}
