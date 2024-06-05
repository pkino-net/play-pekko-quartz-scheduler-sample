package scheduler

import com.google.inject.{AbstractModule, Provides}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}

import java.util.UUID
import javax.inject.Inject

object ImprovedParentActor extends ActorModule {
  sealed trait ImprovedParentActorMessage
  case object Hello extends ImprovedParentActorMessage

  override type Message = ImprovedParentActorMessage

  @Provides
  def create(
      behaviorGenerator: BehaviorGenerator
  ): Behavior[ImprovedParentActorMessage] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage { case Hello =>
      println(ctx.self.path)
      val child = ctx.spawn(
        behaviorGenerator.create(),
        s"ImprovedChildActor${UUID.randomUUID()}"
      )
      child ! ImprovedChildActor.Hello
      Behaviors.same
    }
  }
}

object ImprovedChildActor {
  sealed trait Message
  case object Hello extends Message

  def create(sampleService: SampleService): Behavior[Message] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case Hello =>
        println(ctx.self.path)
        sampleService.exec()
        Behaviors.same
      }
    }
}

class BehaviorGenerator @Inject() (sampleService: SampleService) {
  def create(): Behavior[ImprovedChildActor.Message] =
    ImprovedChildActor.create(sampleService)
}

class ImprovedParentActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    improvedParentActor: ActorRef[
      ImprovedParentActor.ImprovedParentActorMessage
    ]
) {
  schedulerSetting.scheduler.scheduleTyped(
    "Every5Seconds",
    improvedParentActor,
    ImprovedParentActor.Hello
  )
}

class ImprovedParentActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor(ImprovedParentActor, "ImprovedParentActor")
    bind(classOf[ImprovedParentActorScheduler]).asEagerSingleton()
  }
}
