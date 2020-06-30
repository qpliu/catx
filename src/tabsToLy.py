#!/usr/bin/python

import re,sys

histogram = [0]*12

def ly2int(aa):
    a = aa
    ly = 48
    while a.endswith(','):
	a = a[:-1]
	ly -= 12
    while a.endswith("'"):
	a = a[:-1]
	ly += 12
    while a.endswith('es'):
	a = a[:-2]
	ly -= 1
    while a.endswith('is'):
	a = a[:-2]
	ly += 1
    if a=='d':
	ly += 2
    elif a=='e':
	ly += 4
    elif a=='f':
	ly += 5
    elif a=='g':
	ly += 7
    elif a=='a':
	ly += 9
    elif a=='b':
	ly += 11
    elif a!='c':
	raise Exception('Weird note '+aa)
    return ly

scale = "c cis d dis e f fis g gis a ais b".split()

def setScale(a):
    for s in a.split():
	scale[ly2int(s)%12] = s

def setTuning(a):
    global tuning
    tuning = a.split()
    tuning.reverse()
    for i in xrange(1,len(tuning)):
	if (ly2int(tuning[i])>=ly2int(tuning[i-1])):
	    raise Exception('Non decreasing tuning '+a)

def int2ly(a):
    ly = ''
    while a<48:
	a += 12
	ly += ','
    while a>=60:
	a -= 12
	ly += "'"
    note = scale[a-48]
    while note.endswith(',') and ly.startswith("'") or note.endswith("'") and ly.startswith(','):
        note = note[:-1]
        ly = ly[1:]
    return note+ly

def addly(a,b):
    i = ly2int(a)+b
    histogram[i%12] += 1
    return int2ly(i)

setTuning("e, a, d g b e'")

s = None

def find(a,b):
    quote = False
    for i,c in enumerate(a):
	if c=='"':
	    quote = not quote
	if not quote and c==b:
	    return i
    return -1

def split(a,b):
    z = []
    while 1:
	i = find(a,b)
	if i==-1:
	    break
	z.append(a[:i])
	a = a[i+1:]
    z.append(a)
    return z

for line in sys.stdin:
    if line.startswith('% tuning'):
	setTuning(line[8:])
    if line.startswith('% scale'):
	setScale(line[7:])
    out = []
    last = 0
    prev = ''
    for m in re.finditer(r'(?<=\s)((?:(?:(?:[0-9]+-g?x?(?:"[^"]*")?[0-9]+(?:"[^"]*")?~?|g?x?(?:"[^"]*")?[0-9]+(?:"[^"]*")?~?)?\.)*(?:[0-9]+-)?g?x?(?:"[^"]*")?[0-9]+(?:"[^"]*")?~?)?)t(?=[\s123468->~()\\])',line):
	out.append(line[last:m.start(0)])
	last = m.end(0)
	out.append('<')
	tab = m.group(1)
	if not tab:
	    tab = prev
	else:
	    prev = tab
	ss = s
	for a in split(tab,'.'):
	    i = find(a,'-')
	    if i!=-1:
		ss = s = int(a[:i])
		a = a[i+1:]
	    if a:
		if a.startswith('g'):
		    a = a[1:]
		    out.append(r'\parenthesize ')
		if a.startswith('x'):
		    a = a[1:]
		    out.append(r'\deadNote ')
		if a[0]=='"':
		    i = a.index('"',1)
		    out.append(a[1:i])
		    a = a[i+1:]
		tie = ''
		if a.endswith('~'):
		    a = a[:-1]
		    tie = '~'
		after = ''
		if a[-1]=='"':
		    i = a.index('"')
		    after = a[i+1:-1]
		    a = a[:i]
		out.append(addly(tuning[ss-1],int(a))+'\\%d'%ss+tie+after+' ')
	    ss -= 1
	out.append('>')
    out.append(line[last:])
    sys.stdout.write(''.join(out))

bestKeyScore = -1
bestKeyName = None

def checkKey(note,minor):
    global bestKeyScore,bestKeyName
    name = '%s '%int2ly(note+48)
    if minor:
	notes = (0,2,3,5,7,8,10)
	name += 'minor'
    else:
	notes = (0,2,4,5,7,9,11)
	name += 'major'
    score = 0
    for j in notes:
	score += histogram[(i+j)%12]
#    sys.stderr.write('%s %d\n'%(name,score))
    if score>=bestKeyScore:
	if score!=bestKeyScore:
	    bestKeyName = []
	bestKeyScore = score
	bestKeyName.append(name)
    return score

majorScores = []
sumhistogram = sum(histogram)
for i in xrange(12):
    checkKey(i,False)
    majorScores.append(sumhistogram-checkKey(i,True))

sys.stderr.write('Scores: %s\n'%' '.join(map(lambda x:'%d'%x,sorted(majorScores))))
sys.stderr.write('Best key: %s\n'%' or '.join(bestKeyName))
