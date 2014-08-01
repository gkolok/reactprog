package nodescala

import scala.language.postfixOps
import scala.util.{ Try, Success, Failure }
import scala.collection._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.async.Async.{ async, await }
import org.scalatest._
import NodeScala._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NodeScalaSuite extends FunSuite {

  test("A Future should always be created") {
    val always = Future.always(517)

    assert(Await.result(always, 0 nanos) == 517)
  }

  test("A Future should never be created") {
    val never = Future.never[Int]

    try {
      Await.result(never, 1 second)
      assert(false)
    } catch {
      case t: TimeoutException => // ok!
    }
  }

  test("Future.any with one always") {
    val fs = Future.never[Int] :: Future.never[Int] :: Future.always(517) :: Nil

    assert(Await.result(Future.any(fs), 1 second) == 517)

  }

  test("Future.any with two real futures, second should be returned") {
    val fs = List(
      Future.never,
      Future { Thread.sleep(500); 1 },
      Future { Thread.sleep(100); 2 },
      Future { Thread.sleep(300); throw new Error() })

    assert(Await.result(Future.any(fs), 1 second) == 2)

  }

  test("Future.any with two futures and a failing, Failure should be returned") {
    val error = new Error("error from Future")
    val fs = List(
      Future.never,
      Future { Thread.sleep(500); 1 },
      Future { Thread.sleep(300); 2 },
      Future { Thread.sleep(100); throw error })

    try {
      Await.result(Future.any(fs), 1 second)
      assert(false)
    } catch {
      case e: Throwable => assert(e.getCause() == error)
    }

  }

  test("Future.all should return values in the order of initial Futures") {
    val fs = List(
      Future { Thread.sleep(600); 1 },
      Future { Thread.sleep(200); 2 },
      Future { Thread.sleep(300); 3 })

    assert(Await.result(Future.all(fs), 1 second) == List(1, 2, 3))

  }

  test("Future.all should return error if any of the Futures fails") {
    val error = new Error("error from Future")
    val fs = List(
      Future { Thread.sleep(600); 1 },
      Future { Thread.sleep(200); 2 },
      Future { Thread.sleep(100); throw error },
      Future { Thread.sleep(300); 3 })

    try {
      Await.result(Future.any(fs), 1 second)
      assert(false)
    } catch {
      case e: Throwable => assert(e.getCause() == error)
    }

  }

  test("Future.delay should be completed after delay") {
    val result = Await.result(Future.delay(1 second), 1.01 second)
    assert(true)
  }

  test("Future.now should be return value if completed") {
    val r = 1
    val f = Future { r }
    Await.result(f, 0.1 second)
    assert(r == f.now)
  }

  test("Future.now should throw NoSuchElementException if called begore complete") {
    try {
      val f = Future { Thread.sleep(500); 1 }
      f.now
      assert(false)
    } catch {
      case _: NoSuchElementException => assert(true)
    }
  }

  test("Future.now should throw Exception if failed") {
    val error = new Error("error from Future")
    try {
      val f = Future { throw error }
      Thread.sleep(500)
      f.now
      assert(false)
    } catch {
      case e: Throwable => assert(e === error)
    }
  }

  test("Future.delay shuld not be completed before duration") {
    try {
      val result = Await.result(Future.delay(1 second), 0.99 second)
      assert(false)
    } catch {
      case _: TimeoutException => assert(true)
    }
  }

  test("Future.continueWith 1st future should be completed when 2nd starts") {
    val first = Future { Thread.sleep(1000) }
    val second = first.continueWith {
      _ =>
        {
          val firstCompleted = first.isCompleted
          Thread.sleep(500)
          firstCompleted
        }
    }

    assert(Await.result(second, 1510 milliseconds))
  }

  test("Future.continueWith 2 futures should be executed sequentially") {
    val first = Future { Thread.sleep(1000) }
    val second = first.continueWith { _ => Thread.sleep(500) }

    try {
      Await.result(second, 1490 milliseconds)
      assert(false)
    } catch {
      case _: TimeoutException => assert(true)
    }
  }

  test("Future.continueWith 2nd future should be called even if first fails") {
    val first = Future { Thread.sleep(1000); throw new Error("future error") }
    val second = first.continueWith {
      fut =>
        {
          val firstCompleted = first.isCompleted
          Thread.sleep(500)
          firstCompleted
        }
    }

    assert(Await.result(second, 1520 milliseconds))
  }

  test("Future.continue 2nd future should be called even if first fails") {
    val first = Future { Thread.sleep(1000); throw new Error("future error") }
    val second = first.continue {
      tr =>
        {
          val firstCompleted = first.isCompleted
          Thread.sleep(500)
          firstCompleted
        }
    }

    assert(Await.result(second, 1520 milliseconds))
  }

  test("CancellationTokenSource should allow stopping the computation") {
    val cts = CancellationTokenSource()
    val ct = cts.cancellationToken
    val p = Promise[String]()

    async {
      while (ct.nonCancelled) {
        // do work
      }

      p.success("done")
    }

    cts.unsubscribe()
    assert(Await.result(p.future, 1 second) == "done")
  }

  test("Future.run") {
    var finished = false;
    val subscription = Future.run() { ct =>
      Future {
        while (ct.nonCancelled) {
          blocking {
            Thread.sleep(500)
            println("working")
          }
        }
        finished = true
      }
    }
    
    Thread.sleep(2000); 
    subscription.unsubscribe()
    Thread.sleep(510); 
    assert(finished)
  }

  class DummyExchange(val request: Request) extends Exchange {
    @volatile var response = ""
    val loaded = Promise[String]()
    def write(s: String) {
      response += s
    }
    def close() {
      loaded.success(response)
    }
  }

  class DummyListener(val port: Int, val relativePath: String) extends NodeScala.Listener {
    self =>

    @volatile private var started = false
    var handler: Exchange => Unit = null

    def createContext(h: Exchange => Unit) = this.synchronized {
      assert(started, "is server started?")
      handler = h
    }

    def removeContext() = this.synchronized {
      assert(started, "is server started?")
      handler = null
    }

    def start() = self.synchronized {
      started = true
      new Subscription {
        def unsubscribe() = self.synchronized {
          started = false
        }
      }
    }

    def emit(req: Request) = {
      val exchange = new DummyExchange(req)
      if (handler != null) handler(exchange)
      exchange
    }
  }

  class DummyServer(val port: Int) extends NodeScala {
    self =>
    val listeners = mutable.Map[String, DummyListener]()

    def createListener(relativePath: String) = {
      val l = new DummyListener(port, relativePath)
      listeners(relativePath) = l
      l
    }

    def emit(relativePath: String, req: Request) = this.synchronized {
      val l = listeners(relativePath)
      l.emit(req)
    }
  }

  test("Listener should serve the next request as a future") {
    val dummy = new DummyListener(8191, "/test")
    val subscription = dummy.start()

    def test(req: Request) {
      val f = dummy.nextRequest()
      dummy.emit(req)
      val (reqReturned, xchg) = Await.result(f, 1 second)

      assert(reqReturned == req)
    }

    test(immutable.Map("StrangeHeader" -> List("StrangeValue1")))
    test(immutable.Map("StrangeHeader" -> List("StrangeValue2")))

    subscription.unsubscribe()
  }

  test("Server should serve requests") {
    val dummy = new DummyServer(8191)
    val dummySubscription = dummy.start("/testDir") {
      request => for (kv <- request.iterator) yield (kv + "\n").toString
    }

    // wait until server is really installed
    Thread.sleep(500)

    def test(req: Request) {
      val webpage = dummy.emit("/testDir", req)
      val content = Await.result(webpage.loaded.future, 1 second)
      val expected = (for (kv <- req.iterator) yield (kv + "\n").toString).mkString
      assert(content == expected, s"'$content' vs. '$expected'")
    }

    test(immutable.Map("StrangeRequest" -> List("Does it work?")))
    test(immutable.Map("StrangeRequest" -> List("It works!")))
    test(immutable.Map("WorksForThree" -> List("Always works. Trust me.")))

    dummySubscription.unsubscribe()
  }

}




