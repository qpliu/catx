#!/usr/bin/python

import re,subprocess,sys

is_ly = False

def run_espeak1(word):
    if is_ly:
	if word=='' or word=='.' or word=='*' or word=='/' or word=='{' or word=='}' or word[:1]=='!':
	    return word
    p = subprocess.Popen(('espeak','-q','-x',' '+word),stdout=subprocess.PIPE)
    return '('+p.communicate()[0].strip()+')'

def run_espeak(word):
    if not is_ly:
	return run_espeak1(word)
    sb = []
    for a in word.split('|'):
	sb.append(run_espeak1(a))
    return '"'+'|'.join(sb)+'"'

def convert_word(word):
    prefix = ''
    suffix = ''
    if is_ly:
	if word=='--':
	    return ''
	m = re.match(r'([^0-9]*)([0-9]+\.?)$',word)
	if m:
	    word = m.group(1)
	    suffix = m.group(2)
    if word=='+' and not is_ly:
	pass
    elif word[:1]=='\\' and is_ly:
	pass
    else:
	if is_ly and word.startswith('"') and word.endswith('"'):
	    word = word[1:-1]
	if not re.match(r'[0-9*/{}]*$',word):
	    word = run_espeak(word)
    return prefix+word+suffix
	

def convert_line(line):
    global is_ly
    if line.find(r'\lyric')!=-1:
	is_ly = True
	return line
    if is_ly and line.find(r'\mymark')!=-1:
	return line
    if is_ly and line.find(r'\mybar')!=-1:
	return line
    ending = ''
    if is_ly:
	i = line.find('%')
	if i!=-1:
	    ending = line[i:]
	    line = line[:i]
    sb = []
    word = []
    if is_ly:
	quote = False
	for c in line:
	    if quote:
		if c=='"':
		    quote = False
		    word.append(c)
		    sb.append(convert_word(''.join(word)))
		    word = []
		else:
		    word.append(c)
	    elif c=='"':
		sb.append(convert_word(''.join(word)))
		quote = True
		word = ['"']
	    elif c==' ' or c=='\t' or c=='\r' or c=='\n' or c=='{' or c=='}':
		sb.append(convert_word(''.join(word)))
		word = []
		sb.append(c)
	    else:
		word.append(c)
	if quote:
	    raise Exception('bad quote '+line.strip())
    else:
	for c in line:
	    if c=='-' or c==' ' or c=='\t' or c=='\r' or c=='\n':
		sb.append(convert_word(''.join(word)))
		word = []
		sb.append(c)
	    else:
		word.append(c)
    return ''.join(sb)+ending

for line in sys.stdin:
    sys.stdout.write(convert_line(line))
