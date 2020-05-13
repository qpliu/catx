#!/usr/bin/env python

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
	while i<len(line):
	    if line[i]=='%':
		fd.write(line[i:])
		break
	    j = i
	    if line[i]=='`':
		i = line.index('`',j+1)
		word = line[j:i]
		i += 1
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
		fd.write('"'+word[1:]+'"'+duration)
	    else:
		fd.write(lookup(word[1:])+duration+extra)
    fd.close()

dotext('lyrics',True)
dotext('midi',False)

notemapfd.close()
