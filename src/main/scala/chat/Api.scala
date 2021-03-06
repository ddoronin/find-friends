package chat
import akka.actor.typed.ActorRef

object Api {
    trait RoomCommand
    final case class GetSession(token: String, client: ActorRef[SessionCommand]) extends RoomCommand

    sealed trait SessionEvent
    final case class SessionGranted(handle: ActorRef[SessionCommand]) extends SessionCommand

    sealed trait SessionCommand
    final case class PostMessage(message: String) extends SessionCommand
    final case class ReceivedMessage(from: String, message: String) extends SessionCommand
}
