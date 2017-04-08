package nodes.job

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.Config
import common.messages._

class JobNode extends Actor with ActorLogging {
  var Jobs = List(new Job("We are started"))
  override def preStart(): Unit = {
    println("Job node started: " + self.path)
  }
  def receive = {
    case WorkerStarted =>
      System.out.println("Worker at " + sender.path.toString + " alive")
      if (!Jobs.isEmpty) {
        System.out.println("Sending job to " + sender)
        sender ! Jobs.head
        Jobs = Jobs.tail
      } else {
        sender ! "Ping"
      }
  }
}
object JobNode {
  import common.Configurations._
  def main(args: Array[String]) = {
    val config = getConfig("job.conf")
    val jobNodeCf = config.getConfig("job")
    val actorName = jobNodeCf.getString("actorName")
    val sysName = jobNodeCf.getString("actorSystemName")

    val system = ActorSystem(sysName, config)
    val jobNode = system.actorOf(Props[JobNode], name = actorName)
  }
}
