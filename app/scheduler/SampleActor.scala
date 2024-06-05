package scheduler

import com.google.inject.Provides
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.ActorModule

import javax.inject.Inject

object SampleActor extends ActorModule {
  sealed trait SampleActorMessage
  case object Hello extends SampleActorMessage

  override type Message = SampleActorMessage

  @Provides
  def create(sampleService: SampleService): Behavior[SampleActorMessage] =
    Behaviors.receiveMessage {
      case Hello =>
        println("SampleActor received Hello.")
        sampleService.exec()
        Behaviors.same
    }
}

class SampleService @Inject()() {
  def exec(): Unit = println("SampleService was executed.")
}
