package com.example.db

import com.example.config.AppConfig
import com.example.schema.{Order, OrderStatus}
import zio.*

import java.sql.{Connection, DriverManager, ResultSet}

// Raw JDBC wrapped in ZIO — shows ZIO resource management and error channel
trait Database:
  def init: Task[Unit]
  def insert(order: Order): Task[Unit]
  def findById(id: Long): Task[Option[Order]]
  def findAll: Task[List[Order]]
  def updateStatus(id: Long, status: OrderStatus): Task[Unit]

object Database:
  val live: ZLayer[AppConfig, Throwable, Database] =
    ZLayer.fromZIO:
      for
        cfg <- ZIO.serviceWith[AppConfig](_.db)
        _   <- ZIO.attempt(Class.forName(cfg.driver))
      yield DatabaseLive(cfg.url, cfg.user, cfg.password)

  def init: RIO[Database, Unit]                              = ZIO.serviceWithZIO(_.init)
  def insert(o: Order): RIO[Database, Unit]                  = ZIO.serviceWithZIO(_.insert(o))
  def findById(id: Long): RIO[Database, Option[Order]]       = ZIO.serviceWithZIO(_.findById(id))
  def findAll: RIO[Database, List[Order]]                    = ZIO.serviceWithZIO(_.findAll)
  def updateStatus(id: Long, s: OrderStatus): RIO[Database, Unit] =
    ZIO.serviceWithZIO(_.updateStatus(id, s))

private final class DatabaseLive(url: String, user: String, pass: String) extends Database:

  private def withConn[A](f: Connection => A): Task[A] =
    ZIO.acquireReleaseWith(
      ZIO.attempt(DriverManager.getConnection(url, user, pass))
    )(c => ZIO.attempt(c.close()).orDie)(c => ZIO.attempt(f(c)))

  def init: Task[Unit] = withConn: c =>
    c.createStatement().execute(
      """CREATE TABLE IF NOT EXISTS orders (
        |  id      BIGINT PRIMARY KEY,
        |  product VARCHAR(255),
        |  qty     INT,
        |  price   DOUBLE,
        |  status  VARCHAR(50)
        |)""".stripMargin
    )
    ()

  def insert(o: Order): Task[Unit] = withConn: c =>
    val ps = c.prepareStatement(
      "INSERT INTO orders(id, product, qty, price, status) VALUES (?,?,?,?,?)"
    )
    ps.setLong(1, o.id)
    ps.setString(2, o.product)
    ps.setInt(3, o.qty)
    ps.setDouble(4, o.price)
    ps.setString(5, o.status.toString)
    ps.executeUpdate()
    ()

  def findById(id: Long): Task[Option[Order]] = withConn: c =>
    val ps = c.prepareStatement("SELECT * FROM orders WHERE id = ?")
    ps.setLong(1, id)
    val rs = ps.executeQuery()
    if rs.next() then Some(rowToOrder(rs)) else None

  def findAll: Task[List[Order]] = withConn: c =>
    val rs  = c.createStatement().executeQuery("SELECT * FROM orders ORDER BY id")
    val buf = collection.mutable.ListBuffer.empty[Order]
    while rs.next() do buf += rowToOrder(rs)
    buf.toList

  def updateStatus(id: Long, status: OrderStatus): Task[Unit] = withConn: c =>
    val ps = c.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")
    ps.setString(1, status.toString)
    ps.setLong(2, id)
    ps.executeUpdate()
    ()

  private def rowToOrder(rs: ResultSet): Order =
    Order(
      id      = rs.getLong("id"),
      product = rs.getString("product"),
      qty     = rs.getInt("qty"),
      price   = rs.getDouble("price"),
      status  = OrderStatus.valueOf(rs.getString("status"))
    )
