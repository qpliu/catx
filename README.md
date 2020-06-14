# Catx Git
Catx's music and web site.

PDF and MIDI files are in [bin](bin).
Lilypond source is in [src](src).
Web site is in [html/catx.band](html/catx.band).

### Building PDF and MIDI files the easy way

Push your changes and wait for me to build.  Don't worry about minor errors.  As long as I can understand your intent, it is fine.

### Prerequisites to build source the hard way

Lilypond, sh, python, java.

### Building PDF and MIDI files the hard way

If you have a directory called /share/music, the build scripts will crap all over it.  You can either put up with it, rename your /share/music directory or modify the scripts.

Try something like this

```
sudo apt install lilypond bash python openjdk-11-jdk timidity ffmpeg
cd src
./buildAll
```

### Here is a cat

![catx cat](http://catx.band/catx.jpg)

