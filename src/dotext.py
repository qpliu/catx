#!/usr/bin/env python

# What I remember about how this works.  Probably I forgot some features.
# This splits *text files into *text_midi and *text_lyrics files.
# *text_lyrics file is only for putting the texts into lyrics for human to check on PDF.
# *text_midi is the one that actually does stuff.
# You make TextEvents with something like `(some string)`8 to indicate a TextEvent with duration of 1/8th note.
# You can use `(some_string) (another_string)` for two simultaneous TextEvents.

# (some string) has this syntax:
# ([>!.-]*)(\\([^ ()]*\\))?(?:([a-zA-Z]+)=)?([a-zA-Z]+.*)
# group1 is [scope].
# group2 is [keys].
# group3 is [name].
# group4 is [commands].

# [keys] is list of lilypond notes separated by |.  For example (aes|b'|c,)
# [commands] is semi delimimited list of commands.
# If name is present, I can use something like `>name` as abreviation for `>[commands]`.

# The scope is an interval of time, typically from the start of a note to the end of a note.  Notes typically end slightly before their duration.
# [scope] determines scope of command like this:
# -> means scope is from the end of this note until the next `!name` or the end of the song.
# > means the scope is from the start of this note until the next `!name` or the end of the song.
# . means TextEvent only matches notes with the same start and end.

# A note matches the time of a TextEvent with . in scope if start and end times of note exactly match start and end times of TextEvent.
# Otherwise, it matches the time of the TextEvent as long as it has positive overlap in time.

# A note matches the keys of a TextEvent if [keys] is empty or if the note is one of the notes in [keys].

# For a note to match a TextEvent, it must match the keys and the time.
# If more than one TextEvent of a given command note, only the narrower one applies.
# If some has . in scope and other doesn't, one with . is narrower.
# Otherwise if one starts before the other, the one that starts later is narrower.
# Otherwise if one ends before the other, the one that ends earlier is narrower.
# Otherwise if one has [keys] and other doesn't, the one with [keys] is narrower.
# Otherwise, who knows?

import os,re,sys

if len(sys.argv)!=2:
    print 'python %s filename <input'%sys.argv[0]
    sys.exit(1)

noteNames = ('c','cis','d','dis','e','f','fis','g','gis','a','ais','b')
octaveNames = (",,,,",",,,",",,",",","","'","''","'''","''''","'''''","''''''")

def makeNote(i):
    return noteNames[i%12]+octaveNames[i/12]

filename = sys.argv[1]

notemap = dict()
notemapfd = open(filename+'notemap','w')

def lookup(text):
    notes = []
    for word in text.split():
	if word not in notemap:
	    notemap[word] = makeNote(len(notemap))
	    notemapfd.write(word+'\n')
	notes.append(notemap[word])
    if len(notes)==1:
	return notes[0]
    return '<'+' '.join(notes)+'>'

lines = [x for x in sys.stdin]

instruments = set()

def dotext(prefix,isLyrics):
    fd = open(filename+prefix,'w')
    lineno = 0
    duration = None
    for line in lines:
	lineno += 1
	if lineno==1:
	    if isLyrics:
		line = prefix+line+r"\tag #'(textLyrics keep) \new Lyrics \lyricmode"
	    else:
		line = prefix+line
	    fd.write(line)
	    continue
	i = 0
	in_lt_gt = 0
	while i<len(line):
	    if line[i]=='%':
		fd.write(line[i:])
		break
	    j = i
	    if line[i]=='`':
		i = line.index('`',j+1)
		word = line[j:i]
		i += 1
	    elif line[i]=='<' or line[i]=='>':
		if line[i]=='<':
		    in_lt_gt += 1
		else:
		    in_lt_gt -= 1
		fd.write(line[i])
		i += 1
		continue
	    else:
		while line[i]>='a' and line[i]<='z' or line[i]>='A' and line[i]<='Z':
		    i += 1
		if j==i:
		    i += 1
		word = line[j:i]
		if word!='r' and word!='s' and word!='c':
		    fd.write(word)
		    continue
	    j = i
	    while line[j]>='0' and line[j]<='9':
		j += 1
	    if j!=i:
		while line[j]=='.':
		    j += 1
		duration = line[i:j]
		i = j
	    extra = ''
	    while line[i]=='~' or line[i]=='(' or line[i]==')':
		extra += line[i]
		i += 1
	    if word[0]!='`':
		fd.write(r'\skip'+duration)
	    elif isLyrics:
		fd.write('"'+word[1:]+'"')
		if not in_lt_gt:
		    fd.write(duration)
	    else:
		fd.write(lookup(word[1:]))
		if not in_lt_gt:
		    fd.write(duration)
		fd.write(extra)
    fd.close()

dotext('lyrics',True)
dotext('midi',False)

notemapfd.close()
