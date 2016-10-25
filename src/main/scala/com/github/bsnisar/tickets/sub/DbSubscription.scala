package com.github.bsnisar.tickets.sub

import java.sql.Clob

import com.google.common.hash.Hashing
import com.jcabi.xml.XML

class DbSubscription extends Subscriptions {
  override def add(client: String, query: XML): Unit = {
    val xmlQuery = query.toString
    val hashCode = Hashing.md5().hashBytes(xmlQuery.getBytes).toString

  }
}

private object DB {
  import slick.driver.H2Driver.api._

  class Req(tag: Tag) extends Table[(String, String)](tag, "REQ"){
    def hashID = column[String]("HASH_ID", O.PrimaryKey)
    def req = column[String]("REQ")
    def * = (hashID, req)
  }

  val requests = TableQuery[Req]

  class Client(tag: Tag) extends Table[(Long, String)](tag, "CLIENT") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def hash = column[String]("HASH")
    def req = foreignKey("CLIENT_REQ_FK", hash, requests)(_.hashID, onUpdate = ForeignKeyAction.Restrict)
    def * = (id, hash)
  }

  val clients = TableQuery[Client]


  def insertReqIfNotExists(hash: String, xml: String) = requests.forceInsertQuery {
    val exists = (for (req <- requests if req.hashID === hash) yield req).exists
    val insert = (hash, xml)
    for (u <- Query(insert) if !exists) yield u
  }
}