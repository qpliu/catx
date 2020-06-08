# Catx Git
Catx's music and web site.

PDF and MIDI files are in [bin](bin).
Lilypond source is in [src](src).
Web site is in [html/catx.band](html/catx.band).

### Building PDF and MIDI files the easy way

Push your changes and wait for me to build.  Don't worry about minor errors.  As long as I can understand your intent, it is fine.

### Prerequisites to build source the hard way

Lilypond, bash, python, java, timidity, ffmpeg.  Build scripts have my directory structure embedded all over the place.  You will need to change them to work on your computer.  Probably simply deleting every line containing "share" in the files src/build1 and src/buildBin is good enough.

### Building PDF and MIDI files the hard way

```
cd src
vi build1 buildBin
./buildAll
```

### Here is a cat

![catx cat](http://catx.band/catx.jpg)
