package messages

import original.Colour

case object Finish

case object RequestWork

case object WorkAvailable

case object Ray

case object Hit

case object Light

case object Dark

case object Print

case class Work(x: Int, y: Int, width: Int, height: Int)

case class UpdateImage(x: Int, y: Int, colour: Colour)
