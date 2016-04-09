package actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import image.RenderTrait
import messages._
import original.{Light, Ray, _}


/**
  * Renders are workers created by the parent actor RayTracerActor. Renders don't have any state. They do the actual calculations
  * i.e. calculating a colour for a pixel and return the result.
  *
  * @param parent - reference for the parent actor (RayTracerActor) for exchanging messages -- was passed explicitly to
  *               facilitate the unit test of this actor
  * @param objects - information about Scene Objects (scene.objects), derived from a sequence of directives from the input file
  * @param lights - information about Scene Lights (scene.lights), derived from a sequence of directives from the input file
  *
  */

class RenderActor(parent : ActorRef,  val objects: List[Shape], override val lights: List[Light]) extends Actor
  with ActorLogging with RenderTrait {

  /*
   * On initialization a Render (child actor) requests work from the parent actor
   */
  override def preStart {
    parent ! RequestWork
  }

  /*
   * When Render receives work from the sender(parent), it carries out all necessary calculations,
   * sends respective results back to the parents and requests more work
   */

  override def receive = {
    case Work(x, y, width, height) => {
      val colour = renderPixel(x, y, width, height)

      if (Vector(colour.r, colour.g, colour.b).norm < 1) {
        parent ! Dark
      }
      if (Vector(colour.r, colour.g, colour.b).norm >= 1) {
        parent ! Light
      }

      parent ! UpdateImage(x, y, colour);

      parent ! RequestWork
    }
    case WorkAvailable => {
      parent ! RequestWork
    }
  }

  override def updateRayCount: Unit = {
    parent ! Ray
  }

  override def updateHitCount: Unit = {
    parent ! Hit
  }
}