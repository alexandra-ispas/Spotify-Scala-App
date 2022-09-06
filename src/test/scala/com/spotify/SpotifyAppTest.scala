package com.spotify

import com.spotify.database.SpotifyAppQueryRunner
import com.spotify.util.CommandProcessor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayOutputStream

class SpotifyAppTest extends AnyFlatSpec with Matchers {

  behavior of "Spotify App"

  CommandProcessor.connect()

  it should "check connection" in {
    val testQuery = SpotifyAppQueryRunner()
    testQuery.connectToDatabase()
    testQuery.connection.isValid(1) should equal(true)

    testQuery.closeConnection()
    testQuery.connection.isValid(1) should equal(false)
  }

  it should "create a new user" in {
    Spotify.runApp("create user alex")
    CommandProcessor
      .queryRunner.getItemFromTable("user", "username", "\"alex\"")
      .isSuccess should equal(true)
  }

  it should "delete a user" in {
    Spotify.runApp("delete user alex")
    CommandProcessor
      .queryRunner.getItemFromTable("user", "username", "\"alex\"")
      .isFailure should equal(false)
  }

  it should "create a playlist" in {
    Spotify.runApp("create playlist playlist name")
    CommandProcessor
      .queryRunner.getItemFromTable("playlist", "name", "\"playlist name\"")
      .isSuccess should equal(true)
  }

  it should "delete a playlist" in {
    Spotify.runApp("delete playlist playlist name")
    CommandProcessor
      .queryRunner.getItemFromTable("playlist", "name", "\"playlist name\"")
      .isFailure should equal(false)
  }

  it should "add a song to a playlist" in {
    Spotify.runApp("create playlist some playlist")
    Spotify.runApp("add Test to playlist some playlist")

    val songId = CommandProcessor.queryRunner
      .getItemFromTable("song", "name", "\"Test\"")
      .map(_.getInt("id"))

    CommandProcessor
      .queryRunner.getItemFromTable("playlist", "name", "\"some playlist\"")
      .map(x => {
        CommandProcessor
          .queryRunner
          .getItemFromTable("songs_list", "playlist_id", s"${x.getInt("id")}", "song_id")
          .map(y => songId.map(_ should equal(y.getInt("song_id"))))
      })
  }

  it should "print all the songs in an existing playlist" in {
    val outCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Spotify.runApp("list songs in playlist some playlist")
    }
    outCapture.toString.startsWith("Playlist some playlist contains songs:")
  }

  it should "try to print all the songs in a non-existing playlist" in {
    val outCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Spotify.runApp("list songs in playlist something")
    }
    outCapture.toString should equal("Playlist something doesn't exist or is empty.\n")
  }

  it should "process a song" in {
    val outCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Spotify.runApp("play 212")
    }
    outCapture.toString should equal("Song 212 is being played with a duration of 203373.\n")

    outCapture.reset()
    Console.withOut(outCapture) {
      Spotify.runApp("pause")
    }
    outCapture.toString should equal("Paused song 212.\n")

    outCapture.reset()
    Console.withOut(outCapture) {
      Spotify.runApp("resume")
    }
    outCapture.toString should equal("Resumed song 212.\n")

    outCapture.reset()
    Console.withOut(outCapture) {
      Spotify.runApp("stop")
    }
    outCapture.toString should equal("Stopped song 212.\n")
  }


    it should "display the status of the player" in {
      val outCapture = new ByteArrayOutputStream
      Console.withOut(outCapture) {
        Spotify.runApp("status")
      }
      outCapture.toString should equal("No song is currently playing.\n")

      Spotify.runApp("play 1977")
      outCapture.reset()
      Console.withOut(outCapture) {
        Spotify.runApp("status")
      }
      outCapture.toString should equal("Playing song 1977.\n")

      Spotify.runApp("pause")
      outCapture.reset()
      Console.withOut(outCapture) {
        Spotify.runApp("status")
      }
      outCapture.toString should equal("Song 1977 is on pause.\n")

      Spotify.runApp("resume")
      outCapture.reset()
      Console.withOut(outCapture) {
        Spotify.runApp("status")
      }
      outCapture.toString should equal("Playing song 1977.\n")

      Spotify.runApp("stop")
      outCapture.reset()
      Console.withOut(outCapture) {
        Spotify.runApp("status")
      }
      outCapture.toString should equal("No song is currently playing.\n")
    }

  it should "search for keyword" in {
    val outCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Spotify.runApp("search ask")
    }

    outCapture.toString.contains("Your song results:") should equal(true)
  }
}
