package com.zyx2.flac2mp3

import com.zyx2.flac2mp3.AlbumState.AlbumState

case class ConversionState(
  var albumState: AlbumState = AlbumState.NEW_ALBUM,
  var nextAlbum: String = "",
  var prevMp3AlbumPath: String = ""
)

object AlbumState extends Enumeration {
  type AlbumState = Value
  val NEW_ALBUM, EXISTING_ALBUM = Value
}

object Flac2Mp3 {
  def flac2mp3(flacSrc: String, mp3Dest: String): Int =
    new ProcessBuilder(Flac2Mp3Config.lamePath, "-b", s"${Flac2Mp3Config.mp3BitRate}", "-q", s"${Flac2Mp3Config.mp3Quality}", flacSrc, mp3Dest)
    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()
    .waitFor()
}
