package main

import actor.RayTracerActor
import akka.actor._
import reaper.Reaper.WatchMe
import reaper.SystemTerminationReaper

object Execute {

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      println("Usage: numberOfRenders inputFile outputFile <imageWidth> <imageHeight>")
      println("Usage: 100 input.dat output.png 800 600")
      System.exit(1)
    }

    val width = if(args.size < 5) 1900 else args(3).toInt
    val height = if(args.size < 5) 1080 else args(4).toInt

    //initializing the actor system called RayTracer.
    val system = ActorSystem("RayTracer")
    val actorRef = system.actorOf(Props(new RayTracerActor(args(0).toInt, args(1), args(2), width, height)))
    val reaper = system.actorOf(Props(new SystemTerminationReaper()))
    reaper ! WatchMe(actorRef)
  }
}