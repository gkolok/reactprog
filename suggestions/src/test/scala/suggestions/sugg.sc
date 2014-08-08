package suggestions

import rx.lang.scala.Observable
import rx.lang.scala.Notification
import Notification._
object sugg {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  Observable(1, 2, 3).map(n => if (n == 2) throw new Exception() else n).materialize.subscribe(n => n match {
    case OnNext(v) => println("Got value " + v)
    case OnCompleted() => println("Completed")
    case OnError(err) => println("Error: " + err)
  })                                              //> Got value 1
                                                  //| Error: java.lang.Exception
                                                  //| res0: rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$
                                                  //| $anon$1@e25b2fe
}