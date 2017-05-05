package utils.route

import java.util.UUID

import play.api.mvc.PathBindable

import scala.util.control.NonFatal

/**
	* Created by sromic on 05/05/2017.
	*
	* Some route binders.
	*/
object Binders {

  /**
		* A `java.util.UUID` bindable.
		*/
  implicit object UUIDPathBindable extends PathBindable[UUID] {
    def bind(key: String, value: String): Either[String, UUID] =
      try {
        Right(UUID.fromString(value))
      } catch {
        case NonFatal(_) =>
          Left("Cannot parse parameter '" + key + "' with value '" + value + "' as UUID")
      }

    def unbind(key: String, value: UUID): String = value.toString
  }
}
