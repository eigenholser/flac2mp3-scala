package com.zyx2.flac2mp3

import ij.IJ
import ij.process.ImageProcessor

import scala.util.Try

object DestType extends Enumeration {
  type DestType = Value
  val COVER, THUMB = Value
}

object AlbumArt {
  import DestType._
  private val destFormat = "jpg"

  def computeScaleFactor(xAxis: Int, srcSize: Int): Double = xAxis / srcSize.toDouble

  private def makeThumb(ip: ImageProcessor): ImageProcessor = {
    val scaleFactor = computeScaleFactor(Flac2Mp3Config.albumArtResolutionThumb, ip.getWidth)
    ip.resize(Flac2Mp3Config.albumArtResolutionThumb, (scaleFactor * ip.getHeight).toInt)
  }

  private def makeCover(ip: ImageProcessor): ImageProcessor = {
    val scaleFactor = computeScaleFactor(Flac2Mp3Config.albumArtResolutionCover, ip.getWidth)
    ip.resize(Flac2Mp3Config.albumArtResolutionCover, (scaleFactor * ip.getHeight).toInt)
  }

  def scaleImage(src: String, dest: String): Try[Unit] = Try {
    val imp = IJ.openImage(s"$src/${Flac2Mp3Config.albumArtFullName}")
    val ip = imp.getProcessor

    // Disable thumbnail for now. Maybe remove it entirely.
    // imp.processor = makeThumb(ip)
    // IJ.saveAs(imp, destFormat, "$dest/$thumbFilename")

    imp.setProcessor(makeCover(ip))
    IJ.saveAs(imp, destFormat, s"$dest/${Flac2Mp3Config.albumArtCoverName}")
  }

  def scaleImage(src: String, dest: String, destType: DestType): Try[Unit] = Try {
    val imp = IJ.openImage(s"$src/${Flac2Mp3Config.albumArtFullName}")
    val ip = imp.getProcessor

    destType match {
      case THUMB =>
        imp.setProcessor(makeThumb(ip))
        IJ.saveAs(imp, destFormat, s"$dest/${Flac2Mp3Config.albumArtThumbName}")
      case COVER =>
        imp.setProcessor(makeCover(ip))
        IJ.saveAs(imp, destFormat, s"$dest/${Flac2Mp3Config.albumArtCoverName}")
    }
  }
}
