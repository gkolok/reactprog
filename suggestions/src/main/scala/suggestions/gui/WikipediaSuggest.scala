package suggestions
package gui

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing._
import scala.util.{ Try, Success, Failure }
import scala.swing.event._
import swing.Swing._
import javax.swing.UIManager
import Orientation._
import rx.subscriptions.CompositeSubscription
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import observablex._
import search._
import rx.lang.scala.subjects.ReplaySubject
import scala.util.Failure

object WikipediaSuggest extends SimpleSwingApplication with ConcreteSwingApi with ConcreteWikipediaApi {

  {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case t: Throwable =>
    }
  }

  def top = new MainFrame {

    /* gui setup */

    title = "Query Wikipedia"
    minimumSize = new Dimension(900, 600)

    val button = new Button("Get") {
      icon = new javax.swing.ImageIcon(javax.imageio.ImageIO.read(
        this.getClass.getResourceAsStream("/suggestions/wiki-icon.png")))
    }
    val searchTermField = new TextField
    val suggestionList = new ListView(ListBuffer[String]())
    val status = new Label(" ")
    val editorpane = new EditorPane {
      import javax.swing.border._
      border = new EtchedBorder(EtchedBorder.LOWERED)
      editable = false
      peer.setContentType("text/html")
    }

    contents = new BoxPanel(orientation = Vertical) {
      border = EmptyBorder(top = 5, left = 5, bottom = 5, right = 5)
      contents += new BoxPanel(orientation = Horizontal) {
        contents += new BoxPanel(orientation = Vertical) {
          maximumSize = new Dimension(240, 900)
          border = EmptyBorder(top = 10, left = 10, bottom = 10, right = 10)
          contents += new BoxPanel(orientation = Horizontal) {
            maximumSize = new Dimension(640, 30)
            border = EmptyBorder(top = 5, left = 0, bottom = 5, right = 0)
            contents += searchTermField
          }
          contents += new ScrollPane(suggestionList)
          contents += new BorderPanel {
            maximumSize = new Dimension(640, 30)
            add(button, BorderPanel.Position.Center)
          }
        }
        contents += new ScrollPane(editorpane)
      }
      contents += status
    }

    val eventScheduler = SchedulerEx.SwingEventThreadScheduler

    /**
     * Observables
     * You may find the following methods useful when manipulating GUI elements:
     *  `myListView.listData = aList` : sets the content of `myListView` to `aList`
     *  `myTextField.text = "react"` : sets the content of `myTextField` to "react"
     *  `myListView.selection.items` returns a list of selected items from `myListView`
     *  `myEditorPane.text = "act"` : sets the content of `myEditorPane` to "act"
     */

    // TO IMPLEMENT
    val searchTerms: Observable[String] = searchTermField.textValues

    // TO IMPLEMENT
    val suggestions: Observable[Try[List[String]]] =
      searchTerms.sanitized.concatRecovered(wikipediaSuggestionObs)

    // TO IMPLEMENT
    val suggestionSubscription: Subscription = suggestions.observeOn(eventScheduler) subscribe {
      x =>
        x match {
          case Success(list) => suggestionList.listData = list
          case Failure(err) => status.text = err.getMessage()
        }
    }

    // TO IMPLEMENT
    val selections: Observable[String] = button.clicks.map(
      b => if (suggestionList.selection.items.size > 0) suggestionList.selection.items(0) else "").filter(_.size > 0)

    // TO IMPLEMENT
    val pages: Observable[Try[String]] = selections.concatRecovered(wikipediaPageObs)

    // TO IMPLEMENT
    val pageSubscription: Subscription = pages.observeOn(eventScheduler) subscribe {
      x =>
        x match {
          case Success(page) => { editorpane.contentType = "text/html"; editorpane.text = page }
          case Failure(err) => status.text = err.getMessage()
        }
    }

  }

}

trait ConcreteWikipediaApi extends WikipediaApi {
  def wikipediaSuggestion(term: String): Future[List[String]] = Search.wikipediaSuggestion(term)
  def wikipediaSuggestionObs(term: String) = ObservableEx(wikipediaSuggestion(term)).timedOut(10)
  def wikipediaPage(term: String) = Search.wikipediaPage(term)
  def wikipediaPageObs(term: String) = ObservableEx(wikipediaPage(term)).timedOut(10)
}

trait ConcreteSwingApi extends SwingApi {
  type ValueChanged = scala.swing.event.ValueChanged
  object ValueChanged {
    def unapply(x: Event) = x match {
      case vc: ValueChanged => Some(vc.source.asInstanceOf[TextField])
      case _ => None
    }
  }
  type ButtonClicked = scala.swing.event.ButtonClicked
  object ButtonClicked {
    def unapply(x: Event) = x match {
      case bc: ButtonClicked => Some(bc.source.asInstanceOf[Button])
      case _ => None
    }
  }
  type TextField = scala.swing.TextField
  type Button = scala.swing.Button
}
