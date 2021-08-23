#!/usr/bin/python2.7

import random

measures = 1000
lowest = 8
highest = 16
step = 4

with open('generated-random','w') as fd:
    fd.write('randomPart = {\n')
    fd.write('    \\tempo 4 = 60\n')
    note = random.randrange(lowest,highest)
    count = 0;
    while count<measures:
	ly = chr(ord('a')+(7+(note+2)%7)%7)
	for i in xrange(note,0,8):
	    ly += ','
	for i in xrange(7,note,8):
	    ly += "'"
	fd.write('    %s1 |\n'%ly);
	last = note
	while note==last:
	    note = random.randrange(max(lowest,note-step),min(highest,note+step+1))
	count += 1
    fd.write('}\n')
