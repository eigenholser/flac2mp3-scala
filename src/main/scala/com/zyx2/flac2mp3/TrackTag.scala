package com.zyx2.flac2mp3

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.images.StandardArtwork
import org.jaudiotagger.tag.{FieldDataInvalidException, FieldKey, KeyNotFoundException, Tag}

import java.io.{File, IOException}
import java.security.MessageDigest
import java.util.logging.Logger
import scala.util.{Failure, Success, Try}

case class FlacTags(
  artist: String,
  album: String,
  title: String,
  year: String,
  genre: String,
  track: String,
  cddb: String
)

object TrackTag {
  val logger = Logger.getLogger("TrackTag")

  def md5(input: String): String = {
    MessageDigest.getInstance("MD5")
      .digest(input.getBytes())
      .map("%02x".format(_))
      .mkString
  }

  def readFlacTags(flacFile: String): Try[FlacTags] = Try {
    val f = AudioFileIO.read(new File(flacFile))
    val tag = f.getTag

    val artist = tag.getFirst(FieldKey.ARTIST)
    val album = tag.getFirst(FieldKey.ALBUM)
    val title = tag.getFirst(FieldKey.TITLE)
    val year = if (tag.getFirst(FieldKey.YEAR) == "") "0000" else tag.getFirst(FieldKey.YEAR)
    val genre = if (tag.getFirst(FieldKey.GENRE) == "") "None" else tag.getFirst(FieldKey.GENRE)
    val track = tag.getFirst(FieldKey.TRACK)
    val cddb = tag.getFirst("CDDB") match {
      case "" =>
        tag.getFirst("MD5 SIGNATURE") match {
          case "" => md5(title)
          case md5Signature => md5Signature
        }
      case cddbVal => cddbVal
      }

    FlacTags(artist, album, title, year, genre, track, cddb)
  }

  def writeMp3Tags(mp3File: String, mp3AlbumPath: String, flacTags: FlacTags): Unit = {
    val f = try {
      AudioFileIO.read(new File(mp3File))
    } catch {
      case e: Exception =>
        logger.info(s"Unable to read tags on MP3 file: $mp3File: ${e.getMessage}")
        return
    }

    val tag = new ID3v24Tag()
    f setTag tag

    addAlbumArtField(mp3AlbumPath, tag)

    tag.setField(FieldKey.ARTIST, flacTags.artist)
    tag.setField(FieldKey.ALBUM, flacTags.album)
    tag.setField(FieldKey.TITLE, flacTags.title)
    tag.setField(FieldKey.YEAR, flacTags.year)
    tag.setField(FieldKey.GENRE, flacTags.genre)
    tag.setField(FieldKey.TRACK, flacTags.track)
    logger.info(s"Fields finally in mp3 $mp3AlbumPath: ${tag.getFieldCount}")
    // TODO: How does this work?
    //  tag.createField(FieldKey.valueOf("CDDB"), flacTags.cddb)

    f.commit()
  }

  def albumArtTagExists(mp3File: String): Boolean = {
    val f = try {
      AudioFileIO.read(new File(mp3File))
    } catch {
      case e: Exception =>
        logger.info(s"Unable to read tags on MP3 file: $mp3File: ${e.getMessage}")
        return false
    }

    f.getTag match {
      case null => false
      case tag => tag.getFirstArtwork != null
    }
  }

  def addAlbumArtField(mp3AlbumPath: String, tag: Tag): Try[Unit] = Try {
    logger.info(s"Fields initially in mp3 $mp3AlbumPath: ${tag.getFieldCount}")
    val albumArt = StandardArtwork.createArtworkFromFile(
      new File(s"$mp3AlbumPath/${Flac2Mp3Config.albumArtCoverName}")
    )
    tag.addField(albumArt)
    logger.info(s"Fields finally in mp3 $mp3AlbumPath: ${tag.getFieldCount}")
  }

  def deleteAlbumArtField(tag: Tag): Try[Unit] = Try { tag.deleteArtworkField() }

  def updateAlbumArtField(mp3File: String, mp3AlbumPath: String): Unit = {
    val f = try {
      AudioFileIO.read(new File(mp3File))
    } catch {
      case e: Exception =>
        logger.info(s"Unable to read tags on MP3 file: $mp3File: ${e.getMessage}")
        return
    }

    val tag = f.getTag

    if (albumArtTagExists(mp3File)) {
      deleteAlbumArtField(tag) match {
        case Success(_) =>
        case Failure(e: KeyNotFoundException) =>
          logger.info(s"Album art tag not present: ${e.getMessage}")
        case Failure(_) =>
          logger.info("Unexpected error")
      }
    }

    addAlbumArtField(mp3AlbumPath, tag) match {
      case Success(_) =>
        logger.info(s"Album art added to track $mp3File")
      case Failure(e: FieldDataInvalidException) =>
        logger.info(s"Could not tag file with album art: $mp3AlbumPath/${Flac2Mp3Config.albumArtCoverName}: ${e.getMessage}")
      case Failure(e: IOException) =>
        logger.info(s"Could not find album art for tagging: $mp3AlbumPath/${Flac2Mp3Config.albumArtCoverName}: ${e.getMessage}")
      case Failure(_) =>
        logger.info("Unexpected error")
    }

    f.commit()
  }
}
