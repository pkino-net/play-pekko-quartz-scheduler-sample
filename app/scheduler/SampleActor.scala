package scheduler

import com.google.inject.{AbstractModule, Provides}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}

import javax.inject.Inject

object SampleActor extends ActorModule {
  sealed trait SampleActorMessage
  case object Hello extends SampleActorMessage

  override type Message = SampleActorMessage

  @Provides
  def create(sampleService: SampleService): Behavior[SampleActorMessage] =
    Behaviors.receiveMessage { case Hello =>
      println("SampleActor received Hello.")
      sampleService.exec()
      Behaviors.same
    }
}

class SampleService @Inject() () {
  def exec(): Unit = println("SampleService was executed.")
}

class SampleActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    sampleActor: ActorRef[SampleActor.SampleActorMessage]
) {
  schedulerSetting.scheduler.scheduleTyped(
    "Every5Seconds",
    sampleActor,
    SampleActor.Hello
  )
}

class SampleActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor(SampleActor, "SampleActor")
    bind(classOf[SampleActorScheduler]).asEagerSingleton()
  }
}
