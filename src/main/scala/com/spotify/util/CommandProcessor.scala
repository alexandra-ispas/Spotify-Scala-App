package com.spotify.util

import com.spotify.{AlteredTable, CommandRunner}
import com.spotify.database.SpotifyAppQueryRunner

import java.lang.Thread.State
import java.util.concurrent.atomic.AtomicBoolean
import scala.math.pow
import scala.util.{Failure, Success}


object CommandProcessor {
  val queryRunner: SpotifyAppQueryRunner = new SpotifyAppQueryRunner
  var hasToPlay: AtomicBoolean = new AtomicBoolean(true)
  var pause: AtomicBoolean = new AtomicBoolean(false)
  var player: SongPlayer = _

  def connect(): Unit = queryRunner.connectToDatabase()

  def playSong(song: String): SongPlayer = {
    pause.set(false)
    hasToPlay.set(false)
    Thread.sleep(100)

    queryRunner
      .getItemFromTable("song", "name", s"\"$song\"", "duration")
      .map(durationHelper => {
        player = SongPlayer(song, durationHelper.getInt("duration"), System.nanoTime())
        hasToPlay.set(true)
        player.start()})
      .recover(_ => println(s"Song $song doesn't exist."))
    player
  }

  def pauseSong(): Unit = {
    pause.set(true)
    player.duration -= ((System.nanoTime() - player.startTime) / pow(10, 9)).toInt
    println(s"Paused song ${player.name}.")
  }

  def resumeSong(): Unit = {
    pause.set(false)
    player.startTime = System.nanoTime()
    println(s"Resumed song ${player.name}.")
  }

  def stopPlayer(): Unit = {
    hasToPlay.set(false)
    Thread.sleep(200)
  }

  def createPlaylist(playlistName: String): CommandRunner =
    queryRunner
      .insertIntoTable("playlist(user_id, name)", s"${queryRunner.userId}, \"$playlistName\"")

  def createUser(username: String): CommandRunner =
    queryRunner
      .insertIntoTable("user(username)", s"\"$username\"")

  def deleteUser(username: String): AlteredTable = {
    queryRunner
      .getItemFromTable("user", "username", s"\"$username\"")
      .map(idHelper =>
        queryRunner
          .getItemFromTable("playlist", "", "", "name", idHelper.getInt("id"))
          .map(playlistNameHelper => deletePlaylist(playlistNameHelper.getString("name"))))

    queryRunner.removeFromTable("user", "username", s"\"$username\"")
  }

  def addSongToPlaylist(song: String, playlist: String): CommandRunner =
    queryRunner
      .addSongToPlaylist(song, playlist)

  def deletePlaylist(playlistName: String): AlteredTable  =
    queryRunner
      .getItemFromTable("playlist", "name", s"\"$playlistName\"")
      .flatMap(result => {
        queryRunner.removeFromTable("songs_list", "playlist_id", result.getInt("id"))
        queryRunner.removeFromTable("playlist", "name", s"\"$playlistName\"")
      })

  def displayPlaylist(playlistName: String): Unit = {
    val songs = queryRunner.getPlaylistSongs(playlistName)
    songs match {
      case Success(result) =>
        if(!result.isBeforeFirst)
          println(s"Playlist $playlistName doesn't exist or is empty.")
        else
          println(s"Playlist $playlistName contains songs:")
        queryRunner.printer
    }
  }

  def search(keyword: String): Unit =
    queryRunner.searchByKeyword(keyword)

  def displayStatus(): Unit =
    if(player == null ||
      (player.getState != State.TIMED_WAITING && player.getState != State.RUNNABLE))
      println("No song is currently playing.")
    else if(pause.get())
      println(s"Song ${player.name} is on pause.")
    else
      println(s"Playing song ${player.name}.")

  def exitApp(): Unit = {
    println("Spotify App closed.")
    queryRunner.closeConnection()
    System.exit(0)
  }
}
