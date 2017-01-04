package com.github.bsnisar.tickets.sub

import com.google.common.hash.Hashing
import com.jcabi.xml.XML

import slick.driver.H2Driver.api._

class DbSubscription(val db: Database) extends Subscriptions {

  override def add(clientId: String, query: XML): Unit = {
    val queryStr = query.toString
    val hash = Hashing.md5().hashBytes(queryStr.getBytes).toString

    val inserts = DBIO.seq(
      DbSubscription.addReqIfNotExists(hash, queryStr),
      DbSubscription.addClientIfNotExists(clientId, hash)
    )

    db.run(inserts)
  }


}

private object DbSubscription {

  class Req(tag: Tag) extends Table[(String, String)](tag, "REQ"){
    def hashID = column[String]("HASH_ID", O.PrimaryKey)
    def req = column[String]("REQ")
    def * = (hashID, req)
  }

  val requests = TableQuery[Req]

  class Client(tag: Tag) extends Table[(String, String)](tag, "CLIENT") {
    def clientID = column[String]("CLIENT_ID", O.PrimaryKey, O.AutoInc)
    def reqID = column[String]("REQ_ID")
    def req = foreignKey("CLIENT_REQ_FK", reqID, requests)(_.hashID, onUpdate = ForeignKeyAction.Restrict)
    def * = (clientID, reqID)
  }

  val clients = TableQuery[Client]


  def addReqIfNotExists(hash: String, xml: String): DBIOAction[_,NoStream,_] = requests.forceInsertQuery {
    val exists = (for (req <- requests if req.hashID === hash) yield req).exists
    val insert = (hash, xml)
    for (u <- Query(insert) if !exists) yield u
  }

  def addClientIfNotExists(clientId: String, reqId: String): DBIOAction[_,NoStream,_] = clients.forceInsertQuery {
    val matchClient = for {
      client <- clients if client.clientID === clientId && client.reqID === reqId
    } yield client

    val exists = matchClient.exists

    val insert = (clientId, reqId)
    for (u <- Query(insert) if !exists) yield u
  }

}