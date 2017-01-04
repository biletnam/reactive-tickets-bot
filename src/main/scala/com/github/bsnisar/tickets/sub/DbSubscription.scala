package com.github.bsnisar.tickets.sub

import com.google.common.hash.Hashing
import com.jcabi.xml.XML

import slick.driver.H2Driver.api._

class DbSubscription extends Subscriptions {

  override def add(client: String, query: XML): Unit = {
    val queryStr = query.toString
    val hash = Hashing.md5().hashBytes(queryStr.getBytes).toString

    DbSubscription.insert(hash, queryStr, client)
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

  def insert(hash: String, xml: String, clientId: String): DBIOAction[Unit,NoStream,_] = DBIO.seq(
    addReqIfNotExists(hash, xml),
    clients += (clientId, hash)
  )
}