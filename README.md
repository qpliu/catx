# Catx Git
Catx's music and web site.

PDF and MIDI files are in [bin](bin).
Lilypond source is in [src](src).
Web site is in [html/catx.band](html/catx.band).

### Building PDF and MIDI files the easy way

Push your changes and wait for me to build.  Don't worry about minor errors.  As long as I can understand your intent, it is fine.

### Prerequisites to build source the hard way

Lilypond, bash, python, java, timidity, ffmpeg.

### Building PDF and MIDI files the hard way

Build scripts have references to /share/music embedded all over the place.  You need to get rid of them.

Try something like this

```
sudo apt install lilypond bash python openjdk-11-jdk timidity ffmpeg
cd src
for a in src/build1 src/buildBin
do
mv "$a" "$a-orig"
sed <"$a-orig" >"$a" -e 's/^.*\/share\/music.*$/:/'
done
./buildAll
```

### Here is a cat

![catx cat](http://catx.band/catx.jpg)

