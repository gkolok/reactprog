package suggestions

import language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import rx.lang.scala._
import org.scalatest._
import gui._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import rx.concurrency.NewThreadScheduler
import rx.lang.scala.concurrency.Schedulers

@RunWith(classOf[JUnitRunner])
class WikipediaApiTest extends FunSuite {

  object mockApi extends WikipediaApi {
    def wikipediaSuggestion(term: String) = Future {
      if (term.head.isLetter) {
        for (suffix <- List(" (Computer Scientist)", " (Footballer)")) yield term + suffix
      } else {
        List(term)
      }
    }
    def wikipediaPage(term: String) = Future {
      "Title: " + term
    }
  }

  import mockApi._

  test("WikipediaApi should make the stream valid using sanitized") {
    val notvalid = Observable("erik", "erik meijer", "martin")
    val valid = notvalid.sanitized

    var count = 0
    var completed = false

    val sub = valid.subscribe(
      term => {
        assert(term.forall(_ != ' '))
        count += 1
      },
      t => assert(false, s"stream error $t"),
      () => completed = true)
    assert(completed && count == 3, "completed: " + completed + ", event count: " + count)
  }

  test("WikipediaApi should correctly use Recovered") {
    val err = new Error()
    val recovered = Observable(1, 2, 3).map(n => if (n == 2) throw err else n).recovered
    var tries = Seq[Try[Int]]()
    recovered.toSeq.subscribe({ t => tries = t })
    assert(tries === Seq(Try(1), Failure(err)))
  }

  test("timedOut combinator") {
    implicit val scheduler = Schedulers.newThread
    var result = Seq[Int]()
    Observable(1, 2, 3, 4).map(n => {
      Thread.sleep(900);
      n
    }).timedOut(2).toSeq.subscribe({ t => result = t })
    
    Thread.sleep(5000)
    assert(result === Seq(1, 2))
  }
  
  test("WikipediaApi should correctly use concatRecovered") {
    
    val requests = Observable(1, 2, 3)
    val remoteComputation = (n: Int) => Observable(0 to n)
    val responses = requests concatRecovered remoteComputation
    
    val sum = responses.foldLeft(0) { (acc, tn) =>
      tn match {
        case Success(n) => acc + n
        case Failure(t) => throw t
      }
    }
    var total = -1
    val sub = sum.subscribe {
      s => total = s
    }
    assert(total == (1 + 1 + 2 + 1 + 2 + 3), s"Sum: $total")
  }
  
  test("WikipadiaApi concatRecovered should handle failure") {
    
    val requests = Observable(1, 2, 3)
    val error = new Exception
    val remoteComputation = (num: Int) => if (num != 2) Observable(num) else Observable(error)
    val responses = requests concatRecovered remoteComputation
    var tries = Seq[Try[Int]]()
    responses.toSeq.subscribe({ t => tries = t })
    assert(tries === Seq(Success(1), Failure(error), Success(3)))
     
  }
}