package suggestions

import language.postfixOps
import scala.concurrent.duration._
import rx.lang.scala.Observable
import rx.lang.scala.Notification
import Notification._
import scala.util.{ Try, Success, Failure }
import rx.lang.scala.concurrency.Schedulers
import suggestions.observablex._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object sugg {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  Observable(1, 2, 3).map(n => if (n == 2) throw new Exception() else n).materialize.map({
      case OnNext(v) => Success(v)
      case OnError(err) => Failure(err)
    }).subscribe(
    v => println("Got value " + v),
    err => println("Got error"),
    () => println("Completed")
  )                                               //> Got value Success(1)
                                                  //| Got value Failure(java.lang.Exception)
                                                  //| Completed
                                                  //| res0: rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$
                                                  //| $anon$1@598067a5
val f = Future {
  Thread.sleep(1000)
  List("1","2","3")
}                                                 //> f  : scala.concurrent.Future[List[String]] = scala.concurrent.impl.Promise$D
                                                  //| efaultPromise@52feb982
val o: Observable[List[String]] = ObservableEx.apply(f)
                                                  //> o  : rx.lang.scala.Observable[List[String]] = rx.lang.scala.subjects.ReplayS
                                                  //| ubject@307f6b8c
}