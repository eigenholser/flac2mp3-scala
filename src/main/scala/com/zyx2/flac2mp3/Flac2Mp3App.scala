package com.zyx2.flac2mp3

import java.nio.file.{Files, Path, Paths}
import java.nio.file.attribute.FileTime
import scala.util.{Failure, Success}

object Flac2Mp3App extends App {
  val conversionState = ConversionState()

  FlacFiles.fileTree(Flac2Mp3Config.flacRoot)
    .map (convertTrackData)
    .filter(TrackData isThisTrackStale)
    .foreach (processTrack)

  TrackData deleteMp3CoverArt conversionState.prevMp3AlbumPath

  def convertTrackData(path: Path): TrackData = {
    val flacfile = path.toFile.getAbsolutePath
    val fsize = Files.getAttribute(path, "size").asInstanceOf[Long]
    val mtime = Files.getAttribute(path, "lastModifiedTime").asInstanceOf[FileTime]
    TrackData.convertRow(flacfile, fsize, mtime.toMillis)
  }

  def processTrack(trackData: TrackData): Unit = {
    if (conversionState.albumState == AlbumState.EXISTING_ALBUM &&
      conversionState.nextAlbum != trackData.currentAlbum)
      conversionState.albumState = AlbumState.NEW_ALBUM

    if (conversionState.albumState == AlbumState.NEW_ALBUM) {
      TrackData deleteMp3CoverArt conversionState.prevMp3AlbumPath

      conversionState.albumState = AlbumState.EXISTING_ALBUM
      conversionState.nextAlbum = trackData.currentAlbum
      conversionState.prevMp3AlbumPath = trackData.mp3Album

      Files.createDirectories(Paths.get(trackData.mp3Album))

      AlbumArt.scaleImage(
        trackData.flacAlbum,
        trackData.mp3Album,
        DestType.COVER
      )
    }

    if (TrackData.mp3FileExists(trackData) &&
      TrackData.albumArtPNGExists(trackData) &&
      TrackData.isAlbumArtUpdated(trackData)
    )
      TrackTag.updateAlbumArtField(
        mp3File = trackData.mp3File,
        mp3AlbumPath = trackData.mp3Album
      )

    if (!TrackData.isTrackCurrent(trackData)) {
      Files.createDirectories(Paths.get(trackData.mp3Album))
      Flac2Mp3.flac2mp3(
        flacSrc = trackData.flacFile,
        mp3Dest = trackData.mp3File,
      )

      val flacTags = TrackTag.readFlacTags(trackData.flacFile) match {
        case Success(tags) => tags
        case Failure(e) => throw e // Probably want to know about this.
      }

      TrackTag.writeMp3Tags(
        mp3File = trackData.mp3File,
        mp3AlbumPath = trackData.mp3Album,
        flacTags = flacTags
      )
    }
  }
}
