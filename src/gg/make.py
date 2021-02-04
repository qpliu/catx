#!/usr/bin/python
# coding=UTF-8

import random,re

DIVISION=384

N_MEASURES=100

all_drum_filenames=(
# ls ../*/*drs* ../*/*drum*|sort|sed -e 's/^\(.*\)$/    "\1",/'
#    "../ ...And Justice For All/drum",
#    "../ ...And Justice For All/excerptdrums",
#    "../American Autocracy/drscolors",
#    "../American Idiot/drscolors",
    "../Beat It/generated-drscolors",
    "../Big Me/drscolors",
#    "../Black Magic Woman/drum",
    "../Blitzkrieg Bop/drscolors",
    "../Buddy Holly/drs",
#    "../Butterfly/drumscolors",
    "../Check Yes Juliet/drumscolors",
    "../Come As You Are/drscolors",
#    "../Cronies/drums",
#    "../Cronies/generated-drums",
#    "../Cult Of Personality/drums",
#    "../Cult Of Personality/excerptdrums",
#    "../Dancing Queen/drumscolors",
    "../December/drscolors",
    "../Don't Stop Believin'/drscolors",
#    "../Everlong/gen-drums",
#    "../Gimme Chocolate!/generated-drscolors",
#    "../Gimme Some Truth/drums",
#    "../Gimme Some Truth/excerptdrums",
    "../Happy Birthday Kiki/drscolors",
    "../Hardest Button To Button/drscolors",
    "../Have You Ever Seen The Rain/drscolors",
    "../Here Comes Your Man/drscolors",
#    "../Hotel California/generated-drscolors",
    "../I Love Rock'N'Roll/drscolors",
    "../I Won't Back Down/drscolors",
#    "../In The Air Tonight/drscolors",
#    "../Jungle Boogie/drscolors",
    "../Just Like Heaven/drscolors",
#    "../Kashmir/drs",
#    "../Killing In The Name/drums",
#    "../Killing In The Name/excerptdrums",
#    "../Last Dance/drs",
#    "../Let It Be/drums",
#    "../Let It Be/excerptdrums",
    "../Let It Go/generated-drscolors",
#    "../Lump/drums",
#    "../Lump/excerptdrums",
#    "../MOON PRIDE/drs",
#    "../MOON PRIDE/drs_dotext",
    "../Manic Monday/drscolors",
    "../Mary Had A Little Lamb/drumscolors",
#    "../Message In A Bottle/generated-drscolors",
    "../Message In Lipstick/drumscolors",
#    "../Murder City/drums",
#    "../Murder City/excerptdrums",
    "../Oh Pretty Woman/drs",
    "../Our Lips Are Sealed/drumscolors",
    "../Peter's Song/drs",
    "../Rockabye Baby/drumscolors",
#    "../Rockabye Baby/steeldrum",
    "../Rockin Around The Christmas Tree/drumscolors",
#    "../Rockin Around The Christmas Tree/steeldrum",
    "../Row Row Row Your Boat/drscolors",
#    "../School's Out/drscolors",
#    "../Smells Like Teen Spirit/generated-drscolors",
#    "../Talk Dirty To Me/drums",
    "../Talk Dirty To Me/drumssimplecolors",
#    "../The One I Love/drs",
#    "../The World I Know/generated-drscolors",
#    "../Thunderstruck/drums2",
    "../Thunderstruck/drumscolors",
#    "../Vote No/drscolors",
    "../We Will Rock You/drscolors",
#    "../We're In A Pandemic/drs2colors",
#    "../We're In A Pandemic/drscolors",
#    "../You Give Love A Bad Name/drumscolors",
#    "../Ziggy Stardust/gen-drums",
#    "../Z伝説/drums",
#    "../サラバ、愛しき悲しみたちよ/drums",
#    "../サラバ、愛しき悲しみたちよ/drums_dotext",
#    "../ド・キ・ド・キ☆モーニング/drums",
#    "../ド・キ・ド・キ☆モーニング/drums_dotext",
#    "../ムーンライト伝説/drscolors",
    "../ムーンライト伝説/simpledrscolors",
#    "../乙女戦争/drums",
#    "../乙女戦争/drums_dotext",
#    "../行くぜっ!怪盗少女/drs",
#    "../行くぜっ!怪盗少女/drs_dotext",
#    "../走れ!/drs",
)

measures = []

for filename in all_drum_filenames:
    with open(filename) as f:
	for line in f:
	    i = line.find('%')
	    if i!=-1:
		line = line[:i]
	    if line.find('|')!=-1:
		measures.append(line.strip())

random.shuffle(measures)

def getLength(s):
    s = re.sub(r'<<\s*\{(.*)\}\s*\\context.*?>>',r'\1',s)
    l = 0
    n = 0
    hasNote = False
    sb = []
    last = 0
    for m in re.finditer(r'\\tuplet\s*([0-9]+)/([0-9]+)\s*\{([^{}]*)\}',s):
	sb.append(s[last:m.start()])
	last = m.end()
	l += getLength(m.group(3))*int(m.group(2))/int(m.group(1))
    sb.append(s[last:])
    s = ''.join(sb)
    for m in re.finditer(r'(<[^<>]*>|\b[a-z]+(?![a-z]))([0-9]*)(\.?)',s):
	if m.group(1)!='r' and m.group(1)!='s':
	    hasNote = True
	if m.group(2):
	    n = 384*4/int(m.group(2))
	    if m.group(3):
		n += n/2
	if n==0:
	    return 0
	l += n
    if not hasNote:
	return 0
    return l

with open('generated-randomcolors','w') as fd:
    fd.write('randomPart = \\new DrumVoice = "drsb" \\new DrumVoice = "drsa" \\drummode {\n')
    fd.write('    \\tempo 4 = 120\n')
    fd.write('    \\time 4/4\n')
    fd.write('    \\context DrumVoice = "drsa" \\voiceOne\n')
    fd.write('    \\context DrumVoice = "drsb" \\voiceTwo\n')
    count = 0
    for measure in measures:
	l = getLength(measure)
	if l!=384*4:
	    continue
	fd.write(measure+'\n')
	fd.write('    r1 |\n')
	count += 1
	if count==N_MEASURES:
	    break
    fd.write('}\n')
