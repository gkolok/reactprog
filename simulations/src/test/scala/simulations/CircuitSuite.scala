package simulations

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._
import org.scalatest.prop.Checkers
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.Matchers
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite with PropertyChecks {
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

  def orTest(orFunction: (Wire, Wire, Wire) => Unit) = {
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
  }

  //}
  //
  //class CircuitCheck extends Properties("Circuit") with FunSuite {
  //
  //  val sim = new CircuitSimulator with FunSuite {
  //    val InverterDelay = 1
  //    val AndGateDelay = 2
  //    val OrGateDelay = 3
  //  }

  test("demux1") {
    forAll { (in: Wire, c: Wire) =>
      val out1, out2 = new Wire
      demux1(in, c, out1, out2)
      run
      assertDemux1(in, c, out1, out2)
    }
  }

  test("demux 1") {
    forAll { (in: Wire, c: Wire) =>
      val out1, out2 = new Wire
      demux(in, List(c), List(out1, out2))
      run
      assertDemux1(in, c, out1, out2)
    }
  }

  test("demux n if input is false") {
    forAll { (controls: List[Wire]) =>
      val in = new Wire
      in setSignal false
      val outs = setUpDemuxAndRun(in, controls).reverse

      assert(outs.forall(!_.getSignal))
    }
  }

  test("demux n") {
    forAll { (controls: List[Wire]) =>
      val in = new Wire
      in setSignal true
      val outs = setUpDemuxAndRun(in, controls).reverse
      val numOfOutTrue = outs.foldLeft(0) { (sum, w) => sum + toInt(w) }
      assert(numOfOutTrue === 1)

      val controlNum = toInt(controls.reverse)
      val indexes = for ((out, index) <- outs.zipWithIndex if (out.getSignal)) yield index
      val i = indexes.head

      if (!controls.isEmpty)
        assert(controlNum === i, f"output signal is at: $i, but controlNum: $controlNum \nouts: " + outs)
    }
  }

  def setUpDemuxAndRun(in: Wire, controls: List[Wire]): List[Wire] = {
    val outs = genWiresForControls(controls).sample.get
    demux(in, controls, outs)
    run
    outs
  }

  def toInt(w: Wire) = if (w.getSignal) 1 else 0

  def genWiresForControls(cs: List[Wire]) = {
    Gen.listOfN(Math.pow(2, cs.size).toInt, genWire)
  }

  def toInt(cs: List[Wire]): Int = cs.zipWithIndex.foldLeft(0) { (value, wireWithIndex) =>
    val (w, i) = wireWithIndex
    value + (toInt(w) * Math.pow(2, i)).toInt
  }

  lazy val genWire: Gen[Wire] = for {
    signal <- arbitrary[Boolean]
    wire <- new Wire()
  } yield {
    wire setSignal signal
    wire
  }

  implicit lazy val arbWire: Arbitrary[Wire] = Arbitrary(genWire)

  lazy val genLimitedWireList: Gen[List[Wire]] = Gen.choose(0, 3) flatMap { size => Gen.listOfN(size, genWire) }

  implicit lazy val arbWireList: Arbitrary[List[Wire]] = Arbitrary(genLimitedWireList)

  def assertDemux1(in: Wire, c: Wire, out1: Wire, out2: Wire) = {
    val a1 = out1.getSignal == (in.getSignal && c.getSignal)
    assert(a1)
    val a2 = out2.getSignal == (in.getSignal && !c.getSignal)
    assert(a2)
  }
}
