package com.spotify.util

import scala.math.pow

case class SongPlayer(var name: String, var duration: Int, var startTime: Long) extends Thread {
  override def run(): Unit = {

    while (CommandProcessor.pause.get())
      Thread.sleep(100)

    println(s"Song $name is being played with a duration of $duration.")
    while (CommandProcessor.hasToPlay.get() &&
      (System.nanoTime() - startTime) < duration * pow(10, 9)) {

      while (CommandProcessor.pause.get() && CommandProcessor.hasToPlay.get())
        Thread.sleep(100)

      Thread.sleep(100)
    }
    println(s"Stopped song $name.")
  }

}