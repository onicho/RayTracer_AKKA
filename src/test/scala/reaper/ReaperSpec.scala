package reaper

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

class TestReaper(snooper: ActorRef) extends Reaper {
  def allSoulsReaped(): Unit = snooper ! "Dead"
}

class ReaperSpec extends TestKit(ActorSystem("ReaperSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with MustMatchers {
  import Reaper._

  override def afterAll() {
    system.terminate()
  }

  "Reaper" should {
    "work" in {
      // Set up some dummy Actors
      val a = TestProbe()
      val d = TestProbe()

      // Build our reaper
      val reaper = system.actorOf(Props(new TestReaper(testActor)))

      // Watch a couple
      reaper ! WatchMe(a.ref)
      reaper ! WatchMe(d.ref)

      // Stop them
      system.stop(a.ref)
      system.stop(d.ref)

      // Make sure we've been called
      expectMsg("Dead")
    }
  }
}