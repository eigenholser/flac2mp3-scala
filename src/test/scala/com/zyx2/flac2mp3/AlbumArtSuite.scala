package com.zyx2.flac2mp3

import org.scalatest.funsuite.AnyFunSuite

class AlbumArtSuite extends AnyFunSuite {
  test("Scale factor is computed correctly") {
    val expected = AlbumArt.computeScaleFactor(1, 2)
    assert(0.50 == expected)
  }
}
