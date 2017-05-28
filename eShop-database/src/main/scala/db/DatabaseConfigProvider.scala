package db

import slick.jdbc.{JdbcProfile, PostgresProfile}
import currentJdbcProfile.profile

private[db] sealed trait DatabaseConfigProvider[P <: JdbcProfile] {
  val db: P#Backend#Database
}

private[db] trait JdbcDatabaseConfigProvider extends DatabaseConfigProvider[profile.type] {
  import currentJdbcProfile.api.Database

  override lazy val db = Database.forConfig("database")
}

private[db] object currentJdbcProfile {
  val profile = PostgresProfile
  lazy val api = profile.api
}
