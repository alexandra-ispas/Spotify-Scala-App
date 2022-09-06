package com.spotify.config

import com.typesafe.config.{Config, ConfigFactory}

trait LazyConfigParser {

  lazy val config: Config = ConfigFactory.load("application.conf")

}
