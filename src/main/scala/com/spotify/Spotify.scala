package com.spotify

import com.spotify.util.CommandProcessor

import scala.io.StdIn.readLine
import scala.util.{Failure, Success}


object Spotify extends App {

  def runApp(command: String): Unit = command match {

    case "exit" => CommandProcessor.exitApp()

    case "stop" => CommandProcessor.stopPlayer()

    case s"play $song" => CommandProcessor.playSong(song)

    case "pause" => CommandProcessor.pauseSong()

    case "resume" => CommandProcessor.resumeSong()

    case s"create playlist $playlistName" =>
      CommandProcessor.createPlaylist(playlistName) match {
        case Success(_) => println(s"Playlist $playlistName created successfully.")
        case Failure(_) => println(s"Playlist $playlistName already exists.")
      }

    case s"create user $username" =>
      CommandProcessor.createUser(username) match {
        case Success(_) => println(s"User $username created successfully.")
        case Failure(_) => println(s"User $username already exists.")
      }

    case s"delete user $username" =>
      CommandProcessor.deleteUser(username) match {
        case Success(_) => println(s"User $username deleted successfully.")
        case Failure(_) => println(s"User $username doesn't exists.")
      }

    case s"add $songName to playlist $playlistName" =>
      CommandProcessor.addSongToPlaylist(songName, playlistName) match {
        case Success(_) => println(s"Song $songName added to playlist $playlistName successfully.")
        case Failure(_) => println(s"Could not add song $songName to playlist $playlistName.")
      }

    case s"list songs in playlist $playlistName" =>
      CommandProcessor.displayPlaylist(playlistName)

    case s"delete playlist $playlistName" =>
      CommandProcessor.deletePlaylist(playlistName) match {
        case Success(_) => println(s"Playlist $playlistName deleted successfully.")
        case Failure(_) => println(s"Playlist $playlistName doesn't exist.")
      }

    case s"search $keyword" => CommandProcessor.search(keyword)

    case "status" => CommandProcessor.displayStatus()

    case _ => println("Unknown command.")
  }

  CommandProcessor.connect()

  Iterator
    .continually(readLine)
    .foreach(runApp)

}
