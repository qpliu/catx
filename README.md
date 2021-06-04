# Catx Git
Catx's music and web site.

PDF and MIDI files are in [bin](bin).
Lilypond source is in [src](src).
Web site is in [html/catx.band](html/catx.band).

### Prerequisites to build PDF and MIDI files

Lilypond, sh, python, java.

### Building PDF and MIDI files

If you have a directory called /share/music, the build scripts will crap all over it.  You can either put up with it, rename your /share/music directory or modify the scripts.

Try something like this

```
sudo apt install lilypond python openjdk-11-jdk
cd build
./buildAll
```

or

```
cd src/走れ!
../build midi gg
```

or

```
cd src/走れ!
../buildBin
```

### Here is a cat

![catx cat](http://catx.band/catx.jpg)
