package scheduler

import com.google.inject.{AbstractModule, Provides}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}

import java.time.LocalDateTime
import javax.inject.Inject

object StatefulTypedActor extends ActorModule {
  sealed trait StatefulTypedActorMessage
  case object Hello extends StatefulTypedActorMessage

  override type Message = StatefulTypedActorMessage

  @Provides
  def create(
      sampleService: SampleService
  ): Behavior[StatefulTypedActorMessage] = {
    def loop(
        i: Int,
        messageHistory: Seq[(StatefulTypedActorMessage, LocalDateTime)]
    ): Behavior[StatefulTypedActorMessage] =
      Behaviors.receiveMessage { case m @ Hello =>
        println(i)
        println(messageHistory.mkString(",\n"))
        sampleService.exec()
        loop(i + 1, messageHistory :+ (m, LocalDateTime.now()))
      }

    loop(1, Vector())
  }
}

class StatefulTypedActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    statefulTypedActor: ActorRef[StatefulTypedActor.StatefulTypedActorMessage]
) {
  schedulerSetting.scheduler.scheduleTyped(
    "Every5Seconds",
    statefulTypedActor,
    StatefulTypedActor.Hello
  )
}

class StatefulTypedActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor(StatefulTypedActor, "StatefulTypedActor")
    bind(classOf[StatefulTypedActorScheduler]).asEagerSingleton()
  }
}
