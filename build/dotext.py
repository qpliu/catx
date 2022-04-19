#!/usr/bin/env python3

# What I remember about how this works.  Probably I forgot some features.
# This converts *text files into *tex files.
# You make TextEvents with something like `(some string)`8 to indicate a TextEvent with duration of 1/8th note.

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
# . means TextEvent only matches notes with the same start time.

# A note matches the time of a TextEvent with . in scope if start time of note exactly match start time of TextEvent.
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
    print('python3 %s filename'%sys.argv[0])
    sys.exit(1)

noteNames = ('c','cis','d','dis','e','f','fis','g','gis','a','ais','b')
octaveNames = (",,,,",",,,",",,",",","","'","''","'''","''''","'''''","''''''")

def makeNote(i):
    return noteNames[i%12]+octaveNames[i//12]

filename = sys.argv[1]

notemap = dict()
notemapfd = open(filename+'_dotext_notemap','w')

def lookup(text):
    if text not in notemap:
        notemap[text] = makeNote(len(notemap))
        notemapfd.write(text+'\n')
    return notemap[text]

lines = [x for x in open(filename+'_dotext')]

instruments = set()

def dotext():
    fd = open(filename+'_donetext','w')
    for line in lines:
        i = 0
        while i<len(line):
            if line[i]=='%':
                fd.write(line[i:])
                break
            if line[i]!='`':
                fd.write(line[i])
                i += 1
                continue
            j = i+1
            i = line.index('`',j)
            word = line[j:i]
            i += 1
            fd.write(lookup(word))
    fd.close()

dotext()

notemapfd.close()
