package com.zyx2.flac2mp3

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.FilenameUtils

import java.io.File

object Flac2Mp3Config {
  val CONFIG_FILENAME = "flac2mp3.conf"
  val configFilename = FilenameUtils.concat(sys.env("HOME"), CONFIG_FILENAME)
  val config = ConfigFactory.parseFile(new File(configFilename))

  config.root.size match {
    case 0 => throw new Exception(s"Failed to load configuration: $configFilename")
    case size if size > 0 =>
  }

  val flacRoot: String = config.getString("flac_root")
  val mp3Root: String = config.getString("mp3_root")
  val albumArtResolutionThumb: Int = config.getInt("album_art.resolution.thumb")
  val albumArtResolutionCover: Int = config.getInt("album_art.resolution.cover")
  val albumArtFullName: String = config.getString("album_art.name.full")
  val albumArtCoverName: String = config.getString("album_art.name.cover")
  val albumArtThumbName: String = config.getString("album_art.name.thumb")
  val mp3BitRate: Int = config.getInt("mp3.bitrate")
  val mp3Quality: Int = config.getInt("mp3.quality")
  val lamePath: String = config.getString("lame.path")
}
