#!/usr/bin/python

# use something like "make_vocaloid.py <input >output -v voice_name" to specify espeak parameters.

import re,subprocess,sys

argv = sys.argv[1:]

is_jp = False

if len(argv)>0 and argv[0]=='jp':
    is_jp = True
    argv = argv[1:]

is_ly = False

jp_dict = {}
jp_dict[''] = ''
jp_dict['k*'] = 'k'
jp_dict['s*'] = 's'
jp_dict['shi'] = 'Si:'
jp_dict['t*'] = 't'
jp_dict['chi'] = 'tSi:'
jp_dict['tsu'] = 'tsu:'
jp_dict['n*'] = 'n'
jp_dict['h*'] = 'h'
jp_dict['fu'] = 'hu:'
jp_dict['m*'] = 'm'
jp_dict['y*'] = 'j'
jp_dict['r*'] = 'r'
jp_dict['w*'] = 'w'
jp_dict['n'] = 'nnnnnnnnnnnnnnnn'
jp_dict['g*'] = 'g'
jp_dict['z*'] = 'z'
jp_dict['ji'] = 'dZi:'
jp_dict['d*'] = 'd'
jp_dict['ji'] = 'dZi:'
jp_dict['zu'] = 'zu:'
jp_dict['b*'] = 'b'
jp_dict['p*'] = 'p'
jp_dict['ky*'] = 'kj'
jp_dict['sh*'] = 'S'
jp_dict['ch*'] = 'tS'
jp_dict['ny*'] = 'nj'
jp_dict['hy*'] = 'hj'
jp_dict['my*'] = 'mj'
jp_dict['ry*'] = 'rj'
jp_dict['gy*'] = 'gj'
jp_dict['j*'] = 'dZ'
jp_dict['by*'] = 'bj'
jp_dict['py*'] = 'pj'

def run_jp(word):
    w = word.lower()
    if w.endswith('aa') or w.endswith('ii') or w.endswith('uu') or w.endswith('ee') or w.endswith('ou'):
	w = w[:-1]
    got = jp_dict.get(w)
    if got!=None:
	return '('+got+')'
    if w.endswith('a'):
	v = 'A:'
    elif w.endswith('i'):
	v = 'i:'
    elif w.endswith('u'):
	v = 'u:'
    elif w.endswith('e'):
	v = 'E:'
    elif w.endswith('o'):
	v = 'o:'
    else:
	return word
    got = jp_dict.get(w[:-1]+'*')
    if got!=None:
	return '('+got+v+')'
    return word

def run_espeak1(word):
    if word[:1]=='(' and word[-1:]==')':
	return word
    if is_ly:
	if word=='' or word=='.' or word=='*' or word=='/' or word=='{' or word=='}' or word[:1]=='!':
	    return word
    if is_jp:
	return run_jp(word)
    p = subprocess.Popen(['espeak','-q','-x']+sys.argv[1:]+[' '+word],stdout=subprocess.PIPE)
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
