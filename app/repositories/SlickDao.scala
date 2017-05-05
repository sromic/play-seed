package repositories

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

/**
	* Created by sromic on 05/05/2017.
	*
	* Trait that contains generic slick db handling code to be mixed in with DAOs
	*/
trait SlickDao extends DBTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]
