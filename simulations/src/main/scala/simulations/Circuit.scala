package simulations

import common._

class Wire {
  private var sigVal = false
  private var actions: List[Simulator#Action] = List()

  def getSignal: Boolean = sigVal

  def setSignal(s: Boolean) {
    if (s != sigVal) {
      sigVal = s
      actions.foreach(action => action())
    }
  }

  def addAction(a: Simulator#Action) {
    actions = a :: actions
    a()
  }
  
  override 
  def toString = if (getSignal) "1" else "0" 
}

abstract class CircuitSimulator extends Simulator {

  val InverterDelay: Int
  val AndGateDelay: Int
  val OrGateDelay: Int

  def probe(name: String, wire: Wire) {
    wire addAction {
      () =>
        afterDelay(0) {
          println(
            "  " + currentTime + ": " + name + " -> " + wire.getSignal)
        }
    }
  }

  def inverter(input: Wire, output: Wire) {
    def invertAction() {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) { output.setSignal(!inputSig) }
    }
    input addAction invertAction
  }

  def andGate(a1: Wire, a2: Wire, output: Wire) {
    def andAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) { output.setSignal(a1Sig & a2Sig) }
    }
    a1 addAction andAction
    a2 addAction andAction
  }

  //
  // to complete with orGates and demux...
  //

  def orGate(a1: Wire, a2: Wire, output: Wire) {
    def orAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(OrGateDelay) { output.setSignal(a1Sig | a2Sig) }
    }
    a1 addAction orAction
    a2 addAction orAction
  }

  def orGate2(a1: Wire, a2: Wire, output: Wire) {
    val na1, na2, noutput = new Wire
    inverter(a1, na1)
    inverter(a2, na2)
    andGate(na1, na2, noutput)
    inverter(noutput, output)
  }

  def demux1(in: Wire, c: Wire, out1: Wire, out2: Wire) {
    val nc = new Wire();
    andGate(c, in, out1)
    inverter(c, nc)
    andGate(nc, in, out2)
  }
  
  def wire(in: Wire, out: Wire) {
    in addAction { () => afterDelay(0) {out.setSignal(in.getSignal)}  }
  }

  def demux(in: Wire, c: List[Wire], out: List[Wire]) {
    c match {
      case Nil =>
        if (out.size > 1) throw new Error("out size must be exactly 1 if control list is empty.")
        wire(in, out.head)
      case cn :: cs =>
        val out1, out2 = new Wire()
        val (outList1, outList2) = out.splitAt(Math.pow(2, c.size - 1).toInt)
        demux1(in, cn, out1, out2)
        demux(out1, cs, outList1)
        demux(out2, cs, outList2)
    }
  }
}

object Circuit extends CircuitSimulator {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  def andGateExample {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    probe("in1", in1)
    probe("in2", in2)
    probe("out", out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    in1.setSignal(true)
    run

    in2.setSignal(true)
    run
  }

  //
  // to complete with orGateExample and demuxExample...
  //
  
  def demuxExample {
    val in, c, out1, out2 = new Wire
    demux(in, List(c), List(out1, out2))
    probe ("in", in)
    probe("c", c)
    probe("out1", out1)
    probe("out2", out2)
    in.setSignal(true)
    c.setSignal(false)
    run
  }
}

object CircuitMain extends App {
  // You can write tests either here, or better in the test class CircuitSuite.
  Circuit.demuxExample
}
