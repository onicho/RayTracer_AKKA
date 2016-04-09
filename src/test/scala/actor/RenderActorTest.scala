package actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import messages._
import org.scalatest.{MustMatchers, WordSpecLike}
import original.Scene

class RenderActorTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike with MustMatchers {

  "A render actor" must {
    "Request work from its parent when receiving a work available message" in {
      val probe = TestProbe()
      val child = system.actorOf(Props(new RenderActor(probe.ref, null, null)))
      probe.send(child, WorkAvailable)
      probe.expectMsg(RequestWork)
    }

    "When receiving a work message, update the parent with generated color" in {
      val probe = TestProbe()
      val scene = Scene.fromFile("src/test/resources/input.dat")
      val child = system.actorOf(Props(new RenderActor(probe.ref, scene.objects, scene.lights)))
      probe.send(child, Work(1, 1, 10, 10))
      probe.expectMsgAnyClassOf(Dark.getClass, Light.getClass, UpdateImage.getClass, RequestWork.getClass);
    }
  }
}