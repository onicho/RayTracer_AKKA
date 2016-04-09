# Software Design and Programming - Coursework 3
# Completed by students:

* Gisnalbert Spinola
* Henrique Andrella 
* Olya Nicholls
* Renan Salviato


## RAY TRACER


### Implementation

Simple implementation of Ray Tracer, demonstrating the usage of AKKA framework for distributing the work.
For this coursework, it was chosen to use a pull strategy, instead of the more common push one (i.e the workers
request more work when it finishes, instead of the supervisor being the one to distribute the work-load between the
workers). In theory, this approach can scale better in the sense of number of workers/renders.

-The sequential version of the original Ray Tracer was parallelised using the AKKA actor library. 

-Instead of the object Coordinator, a new actor called *RayTracerActor* was introduced.

-*RayTracerActor* is a parent actor to Renders. 

-Renders are the 'workers', i.e. actors that ask the parent for work.They compute colour of a pixel for an image, send the result back to the parent and ask for more work.
 
-The parent actor is collecting all results(pixels processed) from Renders(our worker actors).It updates the image as results are coming through and, when every pixel has been processed, it displays(prints) the image.
 
-Another actor called Reaper was added to the solution. It's core function is to watch/monitor actors in the system,
 so the actor system can be not just stopped but terminated when the job is completed. ref** 

-In the *messages* package it can be seen what messages are being used for communication between actors in the program.

-More detailed documentation is included in the *actor*, *main* and*reaper* packages. 


ref** The solution to "monitor" the main actors and terminate the Akka Actor System was inspired by [Derek Wyatt] 
(https://github.com/derekwyatt), on a reply on StackOverflow. This point does not seem to be very much covered on 
Lightbend documentation, even when forcing the main actor to stop all its children and itself, the actor system 
is still active until the terminate method is invoked. See more comments provided in the documentation under the reaper package. 



### Build

The project is a [SBT] (http://www.scala-sbt.org/) based project, so it can be build with:

```
sbt assembly
```

### Run

After the creation of the uberjar using SBT assembly, the project can be run as any other JVM based JAR.

```
java -jar target/scala-2.11/ray-tracer-assembly-0.0.1.jar 100 src/main/resources/input.dat target/output.png 1900 1080
```

### Test coverage

The main objective was to test the core functionality of the system actors, specifically:

A. That a render actor requests work from its parent when it is available B. When a render receives a work message, it does all necessary calculations and updates the parent with calculated colour 
C. The reaper actor watches actors and terminates the system  

