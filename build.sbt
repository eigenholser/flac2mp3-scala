
name := "flac2mp3-scala"

version := "0.1"

//scalaVersion := "2.13.1"

// jaudio-tagger 2.2.7+ difficult to find.
// https://jitpack.io/#goxr3plus/jaudiotagger
//resolvers += "jitpack" at "https://jitpack.io"
//https://jmaven.com/maven/net.jthink/jaudiotagger

//libraryDependencies += "com.github.goxr3plus" % "jaudiotagger" % "2.2.7"
//libraryDependencies += "com.github.goxr3plus" % "jaudiotagger" % "Tag"
// https://jmaven.com/maven/net.jthink/jaudiotagger
//libraryDependencies += "net.jthink" % "jaudiotagger" % "3.0.1"

libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "net.imagej" % "ij" % "1.53h"
libraryDependencies += "commons-io" % "commons-io" % "2.11.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"