package scheduler

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension

import javax.inject.Inject

class SchedulerSetting @Inject()(actorSystem: ActorSystem) {
  val scheduler: QuartzSchedulerTypedExtension =
    QuartzSchedulerTypedExtension(actorSystem.toTyped)
}
