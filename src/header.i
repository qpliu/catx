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


drumPitchNames.ggone = #'ggone
midiDrumPitches.ggone = f,,, % 17
drumPitchNames.ggtwo = #'ggtwo
midiDrumPitches.ggtwo = fis,,, % 18
drumPitchNames.ggthree = #'ggthree
midiDrumPitches.ggthree = g,,, % 19
drumPitchNames.ggfour = #'ggfour
midiDrumPitches.ggfour = gis,,, % 20
drumPitchNames.ggfive = #'ggfive
midiDrumPitches.ggfive = a,,, % 21
drumPitchNames.ggsix = #'ggsix
midiDrumPitches.ggsix = ais,,, % 22
drumPitchNames.ggseven = #'ggseven
midiDrumPitches.ggseven = b,,, % 23
drumPitchNames.ggeight = #'ggeight
midiDrumPitches.ggeight = c,, % 24

drumPitchNames.ggclick = #'ggclick
midiDrumPitches.ggclick = cis,, % 25
drumPitchNames.ggbell = #'ggbell
midiDrumPitches.ggbell = d,, % 26
drumPitchNames.ggdivision = #'ggdivision
midiDrumPitches.ggdivision = e''' % 88

drumPitchNames.gge = #'gge
midiDrumPitches.gge = f''' % 89
drumPitchNames.ggand = #'ggand
midiDrumPitches.ggand = fis''' % 90
drumPitchNames.gga = #'gga
midiDrumPitches.gga = g''' % 91

drumPitchNames.allone = #'allone
midiDrumPitches.allone = gis''' % 92
drumPitchNames.alltwo = #'alltwo
midiDrumPitches.alltwo = a''' % 93
drumPitchNames.allthree = #'allthree
midiDrumPitches.allthree = ais''' % 94
drumPitchNames.allfour = #'allfour
midiDrumPitches.allfour = b''' % 95
drumPitchNames.allfive = #'allfive
midiDrumPitches.allfive = c'''' % 96
drumPitchNames.allsix = #'allsix
midiDrumPitches.allsix = cis'''' % 97
drumPitchNames.allseven = #'allseven
midiDrumPitches.allseven = d'''' % 98
drumPitchNames.alleight = #'alleight
midiDrumPitches.alleight = dis'''' % 99

drumPitchNames.allclick = #'allclick
midiDrumPitches.allclick = e'''' % 100
drumPitchNames.allbell = #'allbell
midiDrumPitches.allbell = f'''' % 101
drumPitchNames.alldivision = #'alldivision
midiDrumPitches.alldivision = fis'''' % 102

drumPitchNames.alle = #'alle
midiDrumPitches.alle = g'''' % 103
drumPitchNames.alland = #'alland
midiDrumPitches.alland = gis'''' % 104
drumPitchNames.alla = #'alla
midiDrumPitches.alla = a'''' % 105

drumPitchNames.cymcgreen = #'crashcymbal

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


mytweakcolor = #(define-music-function (parser location col x) (color? scheme?) #{ \tweak color #col #x #})

mykavtweakcolor = #(define-music-function (parser location col x) (color? scheme?) #{ #x #})
% IF kav mykavtweakcolor = #(define-music-function (parser location col x) (color? scheme?) #{ \tweak color #col #x #})

mybarNumberCheck = #(define-music-function (parser location nn) (number?) #{ \tag #'(removeWithUnfold keep) \barNumberCheck #nn #})

mymark = #(define-music-function (parser location what nn) (markup? number?) #{ \mybarNumberCheck #nn \mykavtweakcolor #green \mark \markup { \box \bold #what } #})

markpage = #(define-music-function (parser location what) (markup?) #{ \tag #'(pageNumber keep) \mark #what #})
