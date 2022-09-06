package com.spotify.database

import com.spotify.config.LazyConfigParser
import com.spotify.{AlteredTable, QueryResult}
import com.typesafe.config.Config

import java.sql.{Connection, DriverManager, Statement}
import scala.util.Try


trait SpotifyConfig { self: LazyConfigParser =>
  private val spotifyDatabaseLogin: Config = config.getConfig("databaseLogin")
  val url: String = spotifyDatabaseLogin.getString("url")
  val username: String = spotifyDatabaseLogin.getString("username")
  val password: String = spotifyDatabaseLogin.getString("password")
  val userId: Int = spotifyDatabaseLogin.getInt("userid")
}

case class SpotifyAppQueryRunner() extends SpotifyConfig with LazyConfigParser {
  var connection: Connection = _
  var statement: Statement = _

  def connectToDatabase(): Unit = {
    connection = DriverManager.getConnection(url, username, password)
    statement = connection.createStatement()
    statement.execute("use spotify_db")
  }

  def closeConnection(): Unit =
    connection.close()

  def insertIntoTable(tableName: String, values: String): AlteredTable = {
    val query = s"insert into $tableName values ($values)"
    Try (statement.execute(query))
  }

  def removeFromTable(tableName: String, fieldName: String, value: Any): AlteredTable = {
    val query = s"delete from $tableName where $fieldName = $value"
    Try (statement.execute(query))
  }

  def getItemFromTable(tableName: String,
                       fieldName: String,
                       value: String,
                       item: String = "id",
                       userId: Int = userId): QueryResult = {
    Try {
      val query = if (tableName == "playlist" && fieldName != "")
        s"select $item from $tableName where $fieldName=$value and user_id=$userId"
      else if (tableName == "playlist")
        s"select $item from $tableName where user_id=$userId"
      else s"select $item from $tableName where $fieldName=$value"

      statement.executeQuery(query)

      val result = statement.getResultSet
      result.next()
      result
    }
  }

  def addSongToPlaylist(songName: String, playlistName: String): AlteredTable = {
    Try {
      val query = s"insert into songs_list values (" +
        s"(select id from playlist where name = \"$playlistName\" and user_id = $userId)," +
        s"(select id from song where name = \"$songName\"))"
      statement.execute(query)
    }
  }

  def getPlaylistSongs(playlistName: String): QueryResult  = {
    val query = s"select name from song where id in " +
      s"(select song_id from songs_list where playlist_id=" +
      s"(select id from playlist where name=\"$playlistName\" and user_id = $userId))"
    Try (statement.executeQuery(query))
  }

  def printer: Unit = {
    val resultSet = statement.getResultSet
    new Iterator[String] {
      def hasNext: Boolean = resultSet.next()

      def next(): String = resultSet.getString("name")
    }.foreach(x => println("\t" + x))
  }

  def searchByKeyword(keyword: String): Unit = {
    val searchSong = s"select name from song where name like '%$keyword%'"
    val searchArtist = s"select name from artist where name like '%$keyword%'"
    val searchPlaylist = s"select name from playlist where name like '%$keyword%'"

    Try (statement.executeQuery(searchSong))
      .map(_ => {
        println(s"Your song results: "); printer
      })

    Try(statement.executeQuery(searchArtist))
      .map(_ => {
        println(s"Your artist results: "); printer
      })

    Try(statement.executeQuery(searchPlaylist))
      .map(_ => {
        println(s"Your playlist results: "); printer
      })
  }
}
