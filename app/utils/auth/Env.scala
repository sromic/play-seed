package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User

/**
	* Created by sromic on 05/05/2017.
	*
	* The default env.
	*/
trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}
