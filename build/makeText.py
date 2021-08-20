#!/usr/bin/env python2.7

import re,sys

if len(sys.argv)!=1:
    print 'python2.7 %s <input >output'%argv[0]
    sys.exit(1)

braceStack=[]

lineno = 1
for line in sys.stdin:
    ending = ''
    i = line.find('%')
    if i!=-1:
	ending = line[i:]
	line = line[:i]
    braces = 0
    for c in line:
	if c=='{':
	    braces += 1
	if c=='}':
	    braces -= 1
    if braces==1:
	if re.match(r'\s*\\relative\s*\{\s*',line):
	    braceStack.append(True)
	    continue
	braceStack.append(False)
    if braces==-1:
	if braceStack.pop():
	    continue
    spaces = 0
    while line:
	if line[0]==' ':
	    spaces += 1
	    line = line[1:]
	elif line[0]=='\t':
	    spaces = spaces+8&-8
	    line = line[1:]
	else:
	    break
    for b in braceStack:
	if b:
	    spaces -= 4
    while spaces&7:
	spaces -= 1
	line = ' '+line
    while spaces:
	spaces -= 8
	line = '\t'+line
    line = re.sub(r"\b[a-g](?:is|es)?,*'*([0-9]*[^a-zA-Z0-9])",r'c\1',line)
    line = re.sub(r"<[a-gies~,' ]*>",r'c',line)
    line = re.sub(r"\\prall\b",'',line)
    line = re.sub(r"\\staccato\b",'',line)
    line = re.sub(r"\\trill\b",'',line)
    line = re.sub(r"\\startTrillSpan\b",'',line)
    line = re.sub(r"\\stopTrillSpan\b",'',line)
    line = re.sub(r"[_^]\\markup\s*\{[^}]*\}",'',line)
    if line.find('keep')==-1:
	line = re.sub(r"[()]",'',line)
    line = re.sub(r"\\[<>!]",'',line)
    line = re.sub(r"->",'',line)
    line = re.sub(r"\\repeat\s+percent",r'\\repeat unfold',line)
    line = re.sub(r"\\ottava\s*#-?[0-9]+",'',line)
    line = re.sub(r"\bR([0-9]*)\*([0-9]+)",r'\\repeat unfold \2 r\1',line)
    line = re.sub(r'\bc([0-9]*)\^"M"',r'`M`\1',line)
    line = re.sub(r'\bc([0-9]*\.*)\\bendAfterG?\s*#(-?[0-9]+)',r'`bendAfter\2`\1',line)
    line = re.sub(r'\bc([0-9]*\.*)\\chordBendAfterG?\s*#(-?[0-9]+)',r'`bendAfter\2`\1',line)
    line = re.sub(r'\bc([0-9]*\.*)\\bendBeforeG?\s*#(-?[0-9]+)',r'`bendBefore\2`\1',line)
    line = re.sub(r'\bc([0-9]*\.*)\\chordBendBeforeG?\s*#(-?[0-9]+)',r'`bendBefore\2`\1',line)
    line = re.sub(r'\\deadNote\s+c([^a-zA-Z])',r'`deadNote`\1',line)
    if lineno==1:
	line = re.sub(r'^([a-zA-Z]*)Part([a-zA-Z]*)\s*=(.*)\{(\s*)$',r'[TEXT_PREFIX]\1Text\2 = {\4',line)
    sys.stdout.write(line+ending)
    lineno += 1
