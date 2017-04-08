package common.messages

case object Ping
case object WorkerStarted
case class Job(val payload: String)