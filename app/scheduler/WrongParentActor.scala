package scheduler

import com.google.inject.{AbstractModule, Provides}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}

import javax.inject.Inject

object WrongParentActor extends ActorModule {
  sealed trait WrongParentActorMessage
  case object Hello extends WrongParentActorMessage

  override type Message = WrongParentActorMessage

  @Provides
  def create(
      child: ActorRef[WrongChildActor.WrongChildActorMessage]
  ): Behavior[WrongParentActorMessage] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case Hello =>
        println(ctx.self.path)
        child ! WrongChildActor.Hello
        Behaviors.same
      }
    }
}

object WrongChildActor extends ActorModule {
  sealed trait WrongChildActorMessage
  case object Hello extends WrongChildActorMessage

  override type Message = WrongChildActorMessage

  @Provides
  def create(sampleService: SampleService): Behavior[WrongChildActorMessage] = {
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case Hello =>
        println(ctx.self.path)
        sampleService.exec()
        Behaviors.same
      }
    }
  }
}

class WrongActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    wrongParentActor: ActorRef[WrongParentActor.WrongParentActorMessage]
) {
  schedulerSetting.scheduler.scheduleTyped(
    "Every5Seconds",
    wrongParentActor,
    WrongParentActor.Hello
  )
}

class WrongActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor(WrongParentActor, "WrongParentActor")
    bindTypedActor(WrongChildActor, "WrongChildActor")
    bind(classOf[WrongActorScheduler]).asEagerSingleton()
  }
}
