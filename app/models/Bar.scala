package models

import play.api.db._
import play.api.Play.current

case class Bar(id: Long = 0l, name: String = "", summary: String = "", image: Array[Byte] = Array())

object Bar {
  import anorm._
  import anorm.SqlParser._
  //  def apply(id: Long, name: String, summary: String) = new Bar(id = id, name = name, summary = summary)
  //  def unapply(bar: Bar): Option[(Long, String, String)] = Some((bar.id, bar.name, bar.summary))
  //  def unapply(bar: Bar): Option[(Long, String, String, Array[Byte])] = Some((bar.id, bar.name, bar.summary, bar.image))
  val simple = {
    get[Long]("id") ~
      get[String]("name") ~
      get[String]("summary") map {
        case id ~ name ~ summary => Bar(id, name, summary)
      }
  }

  val selectList = SQL("select * from bar")

  def findAll(): List[Bar] = {
    DB.withConnection(implicit connection =>
      selectList.as(Bar.simple *))
  }

  def create(bar: Bar): Unit = {
    DB.withConnection(implicit connecton =>
      SQL("insert into bar(name,summary,image) values({name},{summary},{image})")
        .on('name -> bar.name, "summary" -> bar.summary, "image" -> bar.image).executeUpdate)
  }

  def delete(bar: Bar): Unit = {
    DB.withConnection(implicit connection =>
      SQL("delete from bar where id ={id}").on('id -> bar.id).executeUpdate)
  }

  def getById(id: Long) = {
    DB.withConnection(implicit connection =>
      SQL("select * from bar where id = {id}").on('id -> id).as(Bar.simple *))
  }

  def findBarNameList: List[String] = {
    DB.withConnection {
      implicit connection =>
        selectList().map(row => row[String]("name")).toList
    }
  }

  /**
   * 获取第一个bar记录
   * @return
   */
  def findFirstBar = {
    DB.withConnection({
      implicit connection =>
        SQL("select * from bar").apply().head
    })
  }

  /**
   * 计数
   * @return
   */
  def countBar = {
    DB.withConnection({
      implicit connection =>
        val firstRow = SQL("select count(1) b from bar").apply().head
        firstRow[Long]("b")
    })

  }

  /**
   * 正则匹配
   * @return
   */
  def findBarByPattern = {
    DB.withConnection({
      implicit connection =>
        SQL("select * from bar")().collect({
          case Row(id: Long, name: String) => Bar(id, name)
        })
    })
  }

  /**
   * 特殊的数据类型
   */
  def findBarByClob = {
    DB.withConnection({
      implicit connection =>
        SQL("select id,summary from bar")().map {
          case Row(id: Long, summary: String) => id -> summary
        }
    })
  }

  def findBarByBinary = {
    DB.withConnection({
      implicit connection =>
        SQL("select id,image from bar")().map {
          case Row(id: Long, image: Array[Byte]) => id -> image
        }
    })
  }

}

