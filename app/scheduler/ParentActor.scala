package scheduler

import com.google.inject.{AbstractModule, Provides}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}

import java.util.UUID
import javax.inject.Inject

object ParentActor extends ActorModule {
  sealed trait ParentActorMessage
  case object Hello extends ParentActorMessage

  override type Message = ParentActorMessage

  @Provides
  def create(sampleService: SampleService): Behavior[ParentActorMessage] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case Hello =>
        println(ctx.self.path)
        // 同時に同じ名前で複数のActorを生成できないため、UUIDを名前の一部とする
        val child = ctx.spawn(
          ChildActor.create(sampleService),
          s"ChildActor${UUID.randomUUID()}"
        )
        child ! ChildActor.Hello
        Behaviors.same
      }
    }
}

object ChildActor {
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

class ParentActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    parentActor: ActorRef[ParentActor.ParentActorMessage]
) {
  schedulerSetting.scheduler.scheduleTyped(
    "Every5Seconds",
    parentActor,
    ParentActor.Hello
  )
}

class ParentActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor(ParentActor, "ParentActor")
    bind(classOf[ParentActorScheduler]).asEagerSingleton()
  }
}
