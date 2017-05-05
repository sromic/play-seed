package repositories

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo

import scala.concurrent.Future

/**
	* Created by sromic on 05/05/2017.
	*/
trait PasswordInfoDao {

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]]

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo]

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo]

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo]

  def remove(loginInfo: LoginInfo): Future[Unit]
}
