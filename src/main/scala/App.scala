import java.util.Properties
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.typed.Terminated
import akka.actor.typed.scaladsl.Routers
import chat.ChatRoom
import chat.Api
import chat.Api.SessionGranted
import chat.Api.PostMessage
import chat.Api.ReceivedMessage

object Alice {
  def apply(): Behavior[Api.SessionCommand] = {
    Behaviors.receive{(context, message) => 
      message match {
        case SessionGranted(chat) => {
          chat ! PostMessage("hey there!")
          chat ! PostMessage("This is Alice")
          Behaviors.same
        }
        case ReceivedMessage(from, message) => {
          Behaviors.same
        }
        case _ => ???
      }
    }
  }
}

object Bob {
  def apply(maybeChat: Option[ActorRef[Api.SessionCommand]] = None): Behavior[Api.SessionCommand] = {
    Behaviors.receiveMessage {
      case SessionGranted(chat) => {
        chat ! PostMessage("hey! Bob is here!")
        Bob(Some(chat))
      }
      case ReceivedMessage(from, message) => {
        println(s"$from: $message")
        if (from == "Alice" && message.contains("hey")) {
          maybeChat.foreach(chat => chat ! PostMessage("Hey Alice! Long time no see!"))
        }
        Behaviors.same
      }
      case _ => ???
    }
  }
}

object AppSystem {
  trait Command
  case object Start extends Command

  def apply(): Behavior[Command] = {
    Behaviors.receive{(context, message) =>
      message match {
        case Start => {
          val chatRoom = context.spawn(ChatRoom(), "chat-room")

          val alice = context.spawn(Alice(), "alice-actor")
          chatRoom ! Api.GetSession("Alice", alice)

          val bob = context.spawn(Bob(), "bob-actor")
          chatRoom ! Api.GetSession("Bob", bob)

          Behaviors.same
        }
        case _ => ???
      }
    }
  }
}

object App {
    def main(args: Array[String]): Unit = {
      val system = ActorSystem(AppSystem(), "app-system")
      system ! AppSystem.Start
    }
}
