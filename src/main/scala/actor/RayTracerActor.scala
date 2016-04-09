package actor

import akka.actor.{Actor, ActorLogging, Props}
import messages._
import original.{Image, Scene}
import scala.collection.immutable.Queue


/**
  * The main coordinator actor that reads the input file, takes in instructions about the scene (such as shape lights) and information
  * about the image itself; then it breaks that into small chunks or work and send the work to renders / workers.
  * It collects processed information from workers, puts all info into one image anf displays the image at the very end,
  * when all work is completed.
  * This is a parent actor to all renders.It
  *
  * @param numberOfRenders - a number of actors that will be created for processing pixels, e.g. 100 or 1000 etc
  * @param inputFile - name of the input file that consists of a sequence of directives, one per line, e.g. src/main/resources/input.dat
  * @param outputImageLocation - location directory and name for the output file(the image), e.g target/output.png
  * @param width  - width of the image, pixels e.g. 1080
  * @param height - height of the image, pixels e.g 500
  *
  */

class RayTracerActor(numberOfRenders: Int, inputFile: String, outputImageLocation: String, width: Int, height: Int) extends Actor
  with ActorLogging with RayTracerTrait{


  //A queue of all pixels to be computed for the image is generated

  var queue = Queue[(Int, Int)]()
  for (elem <- for (
    i <- 0 until width;
    j <- 0 until height
  ) yield (i, j)) {
    queue = queue.enqueue(elem)
  }

  private var waiting = width * height   //a number of all the pixels to be computed
  private val scene = Scene.fromFile(inputFile)
  private val renders = (1 to numberOfRenders).map(_ => context.actorOf(Props(new RenderActor(self, scene.objects, scene.lights))))
  private val image = new Image(width, height)


  //Specifying stats results for the image

  def printResults: Unit = {
    println("rays cast " + rayCount)
    println("rays hit " + hitCount)
    println("light " + lightCount)
    println("dark " + darkCount)
    println("Finished")
  }

  //Defying actions upon receiving messages

  override def receive = {
    case Ray => {
      rayCount += 1
    }
    case Hit => {
      hitCount += 1
    }
    case Light => {
      lightCount += 1
    }
    case Dark => {
      darkCount += 1
    }

    /*
     * When a processed pixel received from a render,
     * the image at a certain coordinate gets updated with the computed colour
     * the number of pixels to be processed is reduced by one and when the num is 0,
     * the actor send a message to itself to finish the work
     */

    case UpdateImage(x, y, colour) => {
      image(x, y) = colour
      waiting -= 1
      if (waiting <= 0) {
        self ! Finish
      }
    }

    /*
     * When the Finish message is received the final full image gets printed
     * the parent actor stops all renders and itself
     * image stats get printed
     */

    case Finish => {
      image.print(outputImageLocation)
      renders.foreach(render => context stop render)
      context stop self
      printResults
    }

    /*
     * Worker(Render) is requesting a new pixel to be processed
     * The coordinator actor is dispatching work from the queue
     */

    case RequestWork => {
      queue.dequeueOption match {
        case Some(queueElem) => {
          queue = queueElem._2
          sender ! Work(queueElem._1._1, queueElem._1._2, width, height)
        }
        case None => {
          None
        }
      }
    }
  }
}

trait RayTracerTrait {
  protected var rayCount = 0
  protected var hitCount = 0
  protected var lightCount = 0
  protected var darkCount = 0
}