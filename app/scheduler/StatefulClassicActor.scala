package scheduler

import com.google.inject.AbstractModule
import org.apache.pekko.actor.{Actor, ActorRef}
import play.api.libs.concurrent.PekkoGuiceSupport

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.collection.mutable

class StatefulClassicActor @Inject() (sampleService: SampleService)
    extends Actor {
  private var i: Int = 1
  private val messageHistory
      : mutable.Buffer[(StatefulClassicActor.Message, LocalDateTime)] =
    mutable.Buffer.empty

  override def receive: Receive = { case StatefulClassicActor.Hello =>
    println(i)
    println(messageHistory.mkString(",\n"))
    sampleService.exec()

    i += 1
    messageHistory += ((StatefulClassicActor.Hello, LocalDateTime.now()))
  }
}

object StatefulClassicActor {
  sealed trait Message
  case object Hello extends Message
}

class StatefulClassicActorScheduler @Inject() (
    schedulerSetting: SchedulerSetting,
    @Named("StatefulClassicActor") statefulClassicActor: ActorRef
) {
  schedulerSetting.scheduler.schedule(
    "Every5Seconds",
    statefulClassicActor,
    StatefulClassicActor.Hello
  )
}

class StatefulClassicActorModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindActor[StatefulClassicActor]("StatefulClassicActor")
    bind(classOf[StatefulClassicActorScheduler]).asEagerSingleton()
  }
}
