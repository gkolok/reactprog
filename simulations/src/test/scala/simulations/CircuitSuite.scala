package simulations

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite with Checkers {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5
  
  test("andGate example") {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "and 1")

    in1.setSignal(true)
    run
    
    assert(out.getSignal === false, "and 2")

    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "and 3")
  }

  //
  // to complete with tests for orGate, demux, ...
  //

  def orTest(orFunction: (Wire,Wire,Wire) => Unit) = {
    val in1, in2, out = new Wire
    orFunction(in1, in2, out)
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "or false||false")

    in1.setSignal(true)
    run
    
    assert(out.getSignal === true, "or true||false")

    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "or true||true")

    in1.setSignal(false)
    run
    
    assert(out.getSignal === true, "or false||true")
    
  }
  
  test("orGate") {
    orTest(orGate)
  }

  test("orGate2") {
    orTest(orGate2)
  }
  
  test("demux") {
    check(new CircuitCheck)
  }

}

class CircuitCheck extends Properties("Circuit") with FunSuite {
  
  val sim = new CircuitSimulator with FunSuite {
	  val InverterDelay = 1
	  val AndGateDelay = 2
	  val OrGateDelay =3
  }
  
//  property("demux1") = forAll {(in: Wire, c: Wire) =>
//    val out1, out2 = new Wire
//    sim.demux1(in, c, out1, out2)
//    sim.run
//    assertDemux1(in, c, out1, out2)
//  }
  
  property("demux 1") = forAll {(in: Wire, c: Wire) =>
    val out1, out2 = new Wire
    sim.demux(in, List(c), List(out1, out2))
    sim.run
    assertDemux1(in, c, out1, out2)
  }
  
  lazy val genWire: Gen[Wire] = for {
    signal <- arbitrary[Boolean]
    wire <- new Wire()
  } yield {
    wire setSignal signal
    wire
  }

  implicit lazy val arbWire: Arbitrary[Wire] = Arbitrary(genWire)

  def assertDemux1(in: Wire, c: Wire, out1: Wire, out2: Wire): Boolean = {
    	val a1 = out1.getSignal == (in.getSignal && c.getSignal)
      assert(a1)
      val a2 = out2.getSignal == (in.getSignal && !c.getSignal)
      assert(a2)
      a1 && a2
    }
}

