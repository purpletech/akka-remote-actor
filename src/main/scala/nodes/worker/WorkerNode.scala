package nodes.worker
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorNotFound
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.TimeoutException
import com.typesafe.config.Config
import common.messages._

class WorkerNode(config: Config) extends Actor with ActorLogging {
  val coordinatorCf = config.getConfig("job")
  val coHost = coordinatorCf.getString("host")
  val coPort = coordinatorCf.getString("port")
  val coName = coordinatorCf.getString("actorName")
  val coSysName = coordinatorCf.getString("actorSystemName")
  val coUri = "akka.tcp://" + coSysName + "@" + coHost + ":" + coPort + "/user/" + coName
  println("Coordinator path: " + coUri)

  override def preStart(): Unit = {
    println("Worker created: " + self.path)
    checkForResource
  }

  def checkForResource() {
    val timout = new Timeout(5 seconds)
    try {
      val job = Await.result(context.actorSelection(coUri).resolveOne()(timout), 5 seconds)
      job ! WorkerStarted
    } catch {
      case _: TimeoutException|ActorNotFound(_) => {
        Thread.sleep(5000)
        checkForResource
      }
    }
  }

  def receive = {
    case Job(payload) => {
      println("Receive Job from " + sender.path.toString)
      println(payload)
    }
    case "Ping" => println("Connected with job node " + sender.path.toString)
  }

}
object WorkerNode {
  import common.Configurations._

  def props(config: Config) = Props(new WorkerNode(config))

  def main(args: Array[String]) = {
    val config = getConfig("worker.conf")
    val system = ActorSystem("workerSystem", config)
    val worker = system.actorOf(props(config), "worker")
  }
}