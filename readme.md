# Flac2MP3 Scala

This is a functional implementation of the flac2mp3 utility.

The JAudioTagger dependency must be version 2.2.7 or greater and 
has become unavailable in artifact repositories. There are some
links in `build.sbt` that may be useful. In order to build, a
jar file of JAudioTagger 3.0.1 was downloaded and added to the
project manually.

The configuration file lives in ENV("HOME"):

    flac_root = /my/home/Music/flac2mp3-dev/flac
    mp3_root = /my/home/Music/flac2mp3-dev/mp3
    album_art.resolution.thumb = 200
    album_art.resolution.cover = 1000
    album_art.name.full = album_art.png
    album_art.name.cover = cover.jpg
    album_art.name.thumb = thumb.jpg
    mp3.bitrate = 192
    mp3.quality = 0
    lame.path = /usr/bin/lame