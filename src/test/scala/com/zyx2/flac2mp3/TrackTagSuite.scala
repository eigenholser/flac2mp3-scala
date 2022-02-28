package com.zyx2.flac2mp3

import org.scalatest.funsuite.AnyFunSuite

class TrackTagSuite extends AnyFunSuite {
    test("MD5 hash is correct for given string") {
      val expected = "c10e8df2e378a1584359b0e546cf0149"
      val actual = TrackTag.md5("Hello Hash!")
      assert(expected == actual)
    }

}

//import java.security.MessageDigest
//object StringExtensions {
//  implicit class RichString(val input: String) extends AnyVal {
//    def hash: String = MessageDigest.getInstance("MD5")
//      .digest(input.getBytes)
//      .map("%02x".format(_))
//      .mkString
//  }
//}