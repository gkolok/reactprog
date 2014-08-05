package nodescala

import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

object Main {

  def main(args: Array[String]) {
    // TO IMPLEMENT
    // 1. instantiate the server at 8191, relative path "/test",
    //    and have the response return headers of the request
    val myServer = new NodeScala.Default(8080)
    val myServerSubscription = myServer.start("/test") { 
    		req => (1 to 3).map(i => {blocking { println(s"this respond takes a while...$i"); Thread.sleep(3000)}; s"$i line<br/>"}).iterator
  	}

//    // TO IMPLEMENT
//    // 2. create a future that expects some user input `x`
//    //    and continues with a `"You entered... " + x` message
//    val userInterrupted: Future[String] = ???
//
//    // TO IMPLEMENT
//    // 3. create a future that completes after 20 seconds
//    //    and continues with a `"Server timeout!"` message
//    val timeOut: Future[String] = ???
//
//    // TO IMPLEMENT
//    // 4. create a future that completes when either 10 seconds elapse
//    //    or the user enters some text and presses ENTER
//    val terminationRequested: Future[String] = ???
//
//    // TO IMPLEMENT
//    // 5. unsubscribe from the server
//    terminationRequested onSuccess {
//      case msg => ???
//    }
  }

}