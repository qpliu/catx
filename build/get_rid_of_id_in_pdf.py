#!/usr/bin/env python3

import re,sys

a = sys.stdin.buffer.read()

a = re.compile(b'/ID \[[^][]*\]').sub(b'',a)
a = re.compile(b'/CreationDate\([^()]*\)').sub(b'',a)
a = re.compile(b'/ModDate\([^()]*\)').sub(b'',a)
a = re.compile(b'<xmp:CreateDate>[^<>]*</xmp:CreateDate>').sub(b'',a)
a = re.compile(b'<xmp:ModifyDate>[^<>]*</xmp:ModifyDate>').sub(b'',a)
a = re.compile(b'/URI\([^()]*\)').sub(b'',a)
a = re.compile(b"rdf:about='[^']*'").sub(b'',a)
a = re.compile(b"xapMM:DocumentID='[^']*'").sub(b'',a)
a = re.compile(b"(?sm)^xref\s+[0-9]+ [0-9]+(?:\s+[0-9]+ [0-9]+ [nf])*").sub(b'',a)
a = re.compile(b"(?sm)^startxref\s+[0-9]+").sub(b'',a)

sys.stdout.buffer.write(a)
