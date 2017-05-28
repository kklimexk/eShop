package db.services

import db.DatabaseConfigProvider
import db.currentJdbcProfile.profile
import db.currentJdbcProfile.api._

import shared.models.Entity
import shared.DefaultThreadPool._

import slick.lifted.TableQuery

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class DatabaseService[E <: Entity, K <: profile.Table[E]: ClassTag](cons: Tag => K) { this: DatabaseConfigProvider[profile.type] =>
  val q: TableQuery[K] = TableQuery[K](cons)

  def all(): Future[Seq[E]] = db.run(q.result)
  def insert(e: E): Future[Unit] = db.run(q += e).map(_ => ())
  def findById(id: Long): Future[Option[E]] =
    db.run(findByIdQuery(id).result.headOption)
  def update(id: Long, entity: E): Future[Unit] = {
    db.run(findByIdQuery(id).update(entity)).map(_ => ())
  }
  def deleteAll(): Future[Unit] = {
    db.run(q.delete).map(_ => ())
  }

  def findByIdQuery(id: Long): Query[K, E, Seq] =
    q.filter(m => m.getClass.getMethod("id").invoke(m).asInstanceOf[Rep[Long]] === id)
}
