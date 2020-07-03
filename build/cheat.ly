\version "2.18.2"

\header {
    tagline = ##f
}

#(set-global-staff-size 50)

drumPitchNames.barchimes = #'barchimes
midiDrumPitches.barchimes = c'''
drumPitchNames.noise = #'noise
midiDrumPitches.noise = dis,
drumPitchNames.eperc = #'eperc
midiDrumPitches.eperc = e,
drumPitchNames.tomm = #'himidtom
drumPitchNames.toml = #'highfloortom

#(define myDrumStyleTable
  '((barchimes      xcircle  #f          7)
    (ridecymbal     cross    #f          7)
    (crashcymbal    cross    #f          6)
    (hihat          cross    #f          5)
    (openhihat      cross    "open"      5)
    (closedhihat    cross    "stopped"   5)
    (halfopenhihat  cross    "halfopen"  5)
    (hightom        default  #f          3)
    (himidtom       default  #f          2)
    (eperc          cross    #f          1)
    (snare          default  #f          1)
    (highfloortom   default  #f          0)
    (noise          cross    #f          -1)
    (bassdrum       default  #f          -3)
    ))

\score {
    <<
	\new Staff {
	    \time 14/4
	    \once \override Staff.TimeSignature #'stencil = ##f 
	    \easyHeadsOn
	    \clef bass
	    c,,_"-2" d,,_"-2" e,,_"-2" f,,_"-2" g,,_"-2" a,,_"-2" b,,_"-2"
	    c,_"-1" d,_"-1" e,_"-1" f,_"-1" g,_"-1" a,_"-1" b,_"-1"
	}
	\new Staff {
	    \time 14/4
	    \once \override Staff.TimeSignature #'stencil = ##f 
	    \easyHeadsOn
	    \clef bass
	    c_"0" d_"0" e_"0" f_"0" g_"0" a_"0" b_"0"
	    c'_"1" d'_"1" e'_"1" f'_"1" g'_"1" a'_"1" b'_"1"
	}
	\new Staff {
	    \time 14/4
	    \once \override Staff.TimeSignature #'stencil = ##f 
	    \easyHeadsOn
	    \clef treble
	    c_"0" d_"0" e_"0" f_"0" g_"0" a_"0" b_"0"
	    c'_"1" d'_"1" e'_"1" f'_"1" g'_"1" a'_"1" b'_"1"
	}
	\new Staff {
	    \time 14/4
	    \once \override Staff.TimeSignature #'stencil = ##f 
	    \easyHeadsOn
	    \clef treble
	    c''_"2" d''_"2" e''_"2" f''_"2" g''_"2" a''_"2" b''_"2"
	    c'''_"3" d'''_"3" e'''_"3" f'''_"3" g'''_"3" a'''_"3" b'''_"3"
	}
	\new DrumStaff \with {
	    drumStyleTable = #(alist->hash-table myDrumStyleTable)
	    drumPitchTable = #(alist->hash-table midiDrumPitches)
        } \drummode {
	    \time 14/4
	    \once \override Staff.TimeSignature #'stencil = ##f 
	    barchimes^"barchimes"
	    cymr_"cymr"
	    cymc_"cymc"
	    hh^"hh"
	    hho^"hho"
	    hhc^"hhc"
	    hhho^"hhho"
	    tomh_"tomh"
	    tomm_"tomm"
	    toml_"toml"
	    eperc^"eperc"
	    sn_"sn"
	    noise^"noise"
	    bd_"bd"
	}
    >>
    \layout {
	indent = 0\cm
	short-indent = 0\cm
	\context { \Score \omit BarNumber }
    }
}
