\version "2.18.2"

\header {
    tagline = ##f
}

#(set-global-staff-size 20)

\paper {
% Default is system-system-spacing = #'((basic-distance . 12) (minimum-distance . 8) (padding . 1) (stretchability . 60))
}

\layout {
    \context { \Voice \remove New_fingering_engraver } 
}

colorLyrics = #(define-music-function (parser location color) (list?)
    #{
	\override LyricText.color = #color
	\override LyricHyphen.color = #color
	\override LyricExtender.color = #color
    #}
)

black = \colorLyrics #(rgb-color 0 0 0)
momoka = \colorLyrics #(rgb-color 0 1 0)
ayaka = \colorLyrics #(rgb-color 1 .5 .5)
kanako = \colorLyrics #(rgb-color 1 0 0)
shiori = \colorLyrics #(rgb-color .9 .8 0)
reni = \colorLyrics #(rgb-color .5 0 .5)
akari = \colorLyrics #(rgb-color 0 0 1)

bendBeforeG = #(define-event-function (parser location amount) (number?)
    #{
	^\markup { \hspace #-2 "g" }
    #}
)

bendAfterG = #(define-event-function (parser location amount) (number?)
    #{
	^\markup { \hspace #2 "g" }\bendAfter #amount
    #}
)

chordBendAfterG = #(define-event-function (parser location amount) (number?)
    #{
	^\markup { \hspace #2 "g" }
    #}
)

chordBendAfter = #(define-music-function (parser location amount) (number?)
    #{
    #}
)

startInstrument = #(define-event-function (parser location name) (scheme?)
    #{
	^\markup { \hspace #-1 #(string-append "┌─" name "──") }
    #}
)

stopInstrument = #(define-event-function (parser location name) (scheme?)
    #{
	^\markup { \halign #.5 #(string-append "(" name ")─┐") }
    #}
)

startInstrumentDown = #(define-event-function (parser location name) (scheme?)
    #{
	_\markup { \hspace #-1 #(string-append "└─" name "──") }
    #}
)

stopInstrumentDown = #(define-event-function (parser location name) (scheme?)
    #{
	_\markup { \halign #.5 #(string-append "(" name ")─┘") }
    #}
)

drumPitchNames.metronomeclick = #'metronomeclick
midiDrumPitches.metronomeclick = f,,, % 17
drumPitchNames.metronomebell = #'metronomebell
midiDrumPitches.metronomebell = fis,,, % 18
drumPitchNames.metronomedivision = #'metronomedivision
midiDrumPitches.metronomedivision = g,,, % 19

drumPitchNames.countone = #'countone
midiDrumPitches.countone = gis,,, % 20
drumPitchNames.counttwo = #'counttwo
midiDrumPitches.counttwo = a,,, % 21
drumPitchNames.countthree = #'countthree
midiDrumPitches.countthree = ais,,, % 22
drumPitchNames.countfour = #'countfour
midiDrumPitches.countfour = b,,, % 23
drumPitchNames.countfive = #'countfive
midiDrumPitches.countfive = c,, % 24
drumPitchNames.countsix = #'countsix
midiDrumPitches.countsix = cis,, % 25
drumPitchNames.countseven = #'countseven
midiDrumPitches.countseven = d,, % 26
drumPitchNames.counteight = #'counteight
midiDrumPitches.counteight = e''' % 88
drumPitchNames.counte = #'counte
midiDrumPitches.counte = f''' % 89
drumPitchNames.countand = #'countand
midiDrumPitches.countand = fis''' % 90
drumPitchNames.counta = #'counta
midiDrumPitches.counta = g''' % 91

drumPitchNames.cueone = #'cueone
midiDrumPitches.cueone = gis''' % 92
drumPitchNames.cuetwo = #'cuetwo
midiDrumPitches.cuetwo = a''' % 93
drumPitchNames.cuethree = #'cuethree
midiDrumPitches.cuethree = ais''' % 94
drumPitchNames.cuefour = #'cuefour
midiDrumPitches.cuefour = b''' % 95
drumPitchNames.cuefive = #'cuefive
midiDrumPitches.cuefive = c'''' % 96
drumPitchNames.cuesix = #'cuesix
midiDrumPitches.cuesix = cis'''' % 97
drumPitchNames.cueseven = #'cueseven
midiDrumPitches.cueseven = d'''' % 98
drumPitchNames.cueeight = #'cueeight
midiDrumPitches.cueeight = dis'''' % 99
drumPitchNames.cuee = #'cuee
midiDrumPitches.cuee = e'''' % 100
drumPitchNames.cueand = #'cueand
midiDrumPitches.cueand = f'''' % 101
drumPitchNames.cuea = #'cuea
midiDrumPitches.cuea = fis'''' % 102

#(define myDrumStyleTable '(
    (crashcymbal    cross    #f          6)
    (ridecymbal     cross    #f          5)
    (openhihat      cross    "open"      4)
    (hihat          cross    #f          4)
    (hightom        default  #f          2)
    (snare          default  #f          1)
    (sidestick      cross    #f          1)
    (lowmidtom      default  #f          0)
    (lowtom         default  #f          -1)
    (bassdrum       default  #f          -3)
    (pedalhihat     cross    #f          -5)
))

mytweakcolor = #(define-music-function (parser location col x) (color? scheme?) #{ #x #})
% IF kav mytweakcolor = #(define-music-function (parser location col x) (color? scheme?) #{ \tweak color #col #x #})

mybarNumberCheck = #(define-music-function (parser location nn) (number?) #{ \tag #'(removeWithUnfold keep) \barNumberCheck #nn #})

mymark = #(define-music-function (parser location what nn) (markup? number?) #{ \mybarNumberCheck #nn \mytweakcolor #green \mark \markup { \box \bold #what } #})

markpage = #(define-music-function (parser location what) (markup?) #{ \tag #'(pageNumber keep) \mark #what #})
