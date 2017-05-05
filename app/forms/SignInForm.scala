package forms

import play.api.data.Form
import play.api.data.Forms._

/**
	* Created by sromic on 05/05/2017.
	* The form which handles the submission of the credentials.
	*/
object SignInForm {

  /**
		* A play framework form.
		*/
  val form = Form(
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
		* The form data.
		*
		* @param email The email of the user.
		* @param password The password of the user.
		*/
  final case class Data(
      email: String,
      password: String
  )
}
