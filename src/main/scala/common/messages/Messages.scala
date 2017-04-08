package common.messages

case object Ping
case object Pong
case object WorkerStarted
case class Job(val payload: String)
case object JobRequest
case object NoJob