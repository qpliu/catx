#!/usr/bin/python

import re,subprocess,sys

is_ly = False

def run_espeak(word):
    p = subprocess.Popen(('espeak','-q','-x',word),stdout=subprocess.PIPE)
    word = '('+p.communicate()[0].strip()+')'
    if is_ly:
	word = '"'+word+'"'
    return word

def convert_word(word):
    prefix = ''
    suffix = ''
    if is_ly:
	if word=='--':
	    return ''
	m = re.match(r'([^0-9]+)((?:1|2|4|8|16|32)\.?)$',word)
	if m:
	    word = m.group(1)
	    suffix = m.group(2)
    if word=='\\skip' and is_ly:
	word = '"."'
    elif word=='+' and not is_ly:
	pass
    elif word[:1]=='\\' and is_ly:
	pass
    else:
	if is_ly and word.startswith('"') and word.endswith('"'):
	    prefix += '"'
	    suffix = '"'+suffix
	    word = word[1:-1]
	if not re.match(r'[0-9*/{}]*$',word):
	    word = run_espeak(word)
    return prefix+word+suffix
	

def convert_line(line):
    global is_ly
    if line.find(r'\lyric')!=-1:
	is_ly = True
	return line
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
		sb.append(c)
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
    return ''.join(sb)

for line in sys.stdin:
    sys.stdout.write(convert_line(line))
