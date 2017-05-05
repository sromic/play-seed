package utils

/**
	* Created by sromic on 05/05/2017.
	* Implement this to get a named logger in scope.
	*/
trait Logger {

  /**
		* A named logger instance.
		*/
  val logger = play.api.Logger(this.getClass)
}
