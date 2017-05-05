package repositories

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import scala.concurrent.Future

/**
	* Created by sromic on 05/05/2017.
	*/
trait OAuth2InfoDao {

  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]]

  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info]

  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info]

  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info]

  def remove(loginInfo: LoginInfo): Future[Unit]

}
