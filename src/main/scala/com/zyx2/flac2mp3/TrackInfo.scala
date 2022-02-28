package com.zyx2.flac2mp3

import org.apache.commons.io.{FileUtils, FilenameUtils}

import java.io.File
import java.nio.file.attribute.FileTime
import java.nio.file.{FileSystems, Files, Path, Paths}
import scala.collection.JavaConverters._

object FlacFiles {
  private val FLAC_EXTENSION = ".flac"

  def fileTree(flacRoot: String): Iterator[Path] = {
    val dir = FileSystems.getDefault.getPath(flacRoot)
    Files.walk(dir)
      .iterator()
      .asScala
      .filter(Files.isRegularFile(_))
      .filter(path => path.toString.endsWith(FLAC_EXTENSION))
  }
}

case class TrackData(
  flacFile: String,
  flacAlbum: String,
  currentAlbum: String,
  mp3Album: String,
  mp3File: String,
  fsize: Long,
  mtime: Long
)

object TrackData {
  def apply(
   flacFile: String,
   flacAlbum: String,
   currentAlbum: String,
   mp3File: String,
   mp3Album: String,
   fsize: Long,
   mtime: Long
 ): TrackData = new TrackData(
    flacFile,
    flacAlbum,
    currentAlbum,
    mp3Album,
    mp3File,
    fsize,
    mtime
  )

  def convertRow(flacFile: String, fsize: Long, mtime: Long): TrackData = {
    val MP3_EXTENSION = "mp3"
    val EXTENSION_SEPARATOR = FilenameUtils.EXTENSION_SEPARATOR_STR
    val DIRECTORY_SEPARATOR = "/" // TODO: What?! No FilenameUtils platform independent constant?
    val flacAlbumPath = FilenameUtils.getFullPathNoEndSeparator(flacFile)
    val flacTrackName = FilenameUtils.getName(flacFile)
    val trackName = FilenameUtils.getBaseName(flacTrackName)
    val mp3TrackName = trackName + EXTENSION_SEPARATOR + MP3_EXTENSION
    val currentAlbum = flacFile
      .replaceFirst(Flac2Mp3Config.flacRoot + DIRECTORY_SEPARATOR, "")
      .replace(DIRECTORY_SEPARATOR + flacTrackName, "")
    val mp3Album = FilenameUtils.concat(Flac2Mp3Config.mp3Root, currentAlbum)
    val mp3File = FilenameUtils.concat(mp3Album, mp3TrackName)

    TrackData(
      flacFile = flacFile,
      flacAlbum = flacAlbumPath,
      currentAlbum = currentAlbum,
      mp3Album = mp3Album,
      mp3File = mp3File,
      fsize = fsize,
      mtime = mtime
    )
  }

  def mp3FileExists(trackData: TrackData): Boolean = new File(trackData.mp3File).exists

  def albumArtPNGExists(trackData: TrackData): Boolean =
    Paths.get(trackData.flacAlbum).resolve(Flac2Mp3Config.albumArtFullName).toFile.exists

  def isTrackCurrent(trackData: TrackData): Boolean =
    mp3FileExists(trackData) match {
      case true =>
        val flacMtime = Files.getAttribute(Paths.get(trackData.flacFile), "lastModifiedTime").asInstanceOf[FileTime]
        val mp3Mtime = Files.getAttribute(Paths.get(trackData.mp3File), "lastModifiedTime").asInstanceOf[FileTime]
        flacMtime.toMillis < mp3Mtime.toMillis
      case false => false
    }

  def isAlbumArtUpdated(trackData: TrackData): Boolean =
  mp3FileExists(trackData) && albumArtPNGExists(trackData) match {
    case true =>
      val albumArtMtime = Files.getAttribute(
        Paths.get(trackData.flacAlbum).resolve(Flac2Mp3Config.albumArtFullName), "lastModifiedTime"
      ).asInstanceOf[FileTime]
      val mp3Mtime = Files.getAttribute(
        Paths.get(trackData.mp3File), "lastModifiedTime"
      ).asInstanceOf[FileTime]
      TrackTag.albumArtTagExists(trackData.mp3File) &&
        albumArtMtime.toMillis > mp3Mtime.toMillis ||
        !TrackTag.albumArtTagExists(trackData.mp3File)
    case false => false
  }

  def isThisTrackStale(trackData: TrackData): Boolean =
    !isTrackCurrent(trackData) || isAlbumArtUpdated(trackData)

  def deleteMp3CoverArt(mp3AlbumPathAbsolute: String): Boolean =
    mp3AlbumPathAbsolute match {
      case path if path != null =>
        new File(FilenameUtils.concat(path, Flac2Mp3Config.albumArtCoverName)).delete()
      case null => false
    }
}


