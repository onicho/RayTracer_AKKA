package image

import original._

trait RenderTrait {

  val objects: List[Shape]
  val lights: List[Light]

  def updateRayCount
  def updateHitCount

  private val antiAliasingFactor = 4


  //TODO: Validate all this
  val ambient = .2f
  val background = Colour.black
  val eye = Vector.origin
  val angle = 90f
  // viewing angle
  //val angle = 180f // fisheye
  val frustum = (.5 * angle * math.Pi / 180).toFloat
  val cosf = math.cos(frustum)
  val sinf = math.sin(frustum)
  // Anti-aliasing parameter -- divide each pixel into sub-pixels and
  // average the results to get smoother images.
  val ss = antiAliasingFactor


  def renderPixel(x: Int, y: Int, width: Int, height: Int): Colour = {
    var colour = Colour.black

    for (dx <- 0 until ss) {
      for (dy <- 0 until ss) {

        // Create a vector to the pixel on the view plane formed when
        // the eye is at the origin and the normal is the Z-axis.
        val dir = Vector(
          (sinf * 2 * ((x + dx.toFloat / ss) / width - .5)).toFloat,
          (sinf * 2 * (height.toFloat / width) * (.5 - (y + dy.toFloat / ss) / height)).toFloat,
          cosf.toFloat).normalized

        val c = trace(Ray(eye, dir)) / (ss * ss)
        colour += c
      }
    }
    return colour
  }


  //TODO: refactor this part

  def shadow(ray: Ray, l: Light): Boolean = {
    val distSquared = (l.loc - ray.orig).normSquared
    intersections(ray).foreach {
      case (v, o) =>
        if ((v - ray.orig).normSquared < distSquared)
          return true
    }
    false
  }

  // Compute the color contributed by light l at point v on object o.
  def shade(ray: Ray, l: Light, v: Vector, o: Shape): Colour = {
    val toLight = Ray(v, (l.loc - v).normalized)

    val N = o.normal(v)

    // Diffuse light
    if (shadow(toLight, l) || (N dot toLight.dir) < 0)
      Colour.black
    else {
      // diffuse light
      val diffuse = o.colour * (N dot toLight.dir)

      println("ray " + ray)
      println("diffuse " + diffuse)

      // specular light
      val R = reflected(-toLight.dir, N)
      val len = ray.dir.norm * R.norm

      val specular = if (len > 1e-12) {
        // Want the angle between R and the vector TO the eye

        val cosalpha = -(ray.dir dot R) / len

        val power = if (cosalpha > 1e-12) math.pow(cosalpha, o.shine).toFloat else 0.0f

        if (power > 1e-12) {
          val scale = o.reflect * power
          l.colour * o.specular * scale
        }
        else
          Colour.black
      }
      else
        Colour.black

      println("specular " + specular)

      val color = diffuse + specular

      println("color " + color + " 0x" + color.rgb.toHexString)

      color
    }
  }

  def reflected(v: Vector, N: Vector): Vector = v - (N * 2.0f * (v dot N))

  def intersections(ray: Ray) = objects.flatMap {
    o => o.intersect(ray).map { v => (v, o) }
  }

  def closestIntersection(ray: Ray) = intersections(ray).sortWith {
    case ((v1, o1), (v2, o2)) => {
      val q1 = (v1 - ray.orig).normSquared
      val q2 = (v2 - ray.orig).normSquared
      q1 < q2
    }
  }.headOption

  val maxDepth = 3

  def trace(ray: Ray): Colour = trace(ray, maxDepth)

  private def trace(ray: Ray, depth: Int): Colour = {
    updateRayCount

    // Compute the intersections of the ray with every object, sort by
    // distance from the ray's origin and pick the closest to the origin.
    val r = closestIntersection(ray)

    r match {
      case None => {
        // If no intersection, the color is black
        background
      }
      case Some((v, o)) => {
        // Compute the color as the sum of:

        updateHitCount

        // The contribution of each point light source.
        var c = lights.foldLeft(Colour.black) {
          case (c, l) => c + shade(ray, l, v, o)
        }

        // The contribution of the ambient light.
        c += o.colour * ambient

        // Return the color.
        c
      }
    }
  }
}