#!/usr/bin/env python

import re,sys

a = sys.stdin.read()

a = re.sub(r'/ID \[[^][]*\]',r'',a)
a = re.sub(r'/CreationDate\([^()]*\)',r'',a)
a = re.sub(r'/ModDate\([^()]*\)',r'',a)
a = re.sub(r'<xmp:CreateDate>[^<>]*</xmp:CreateDate>',r'',a)
a = re.sub(r'<xmp:ModifyDate>[^<>]*</xmp:ModifyDate>',r'',a)
a = re.sub(r'/URI\([^()]*\)',r'',a)
a = re.sub(r"rdf:about='[^']*'",r'',a)
a = re.sub(r"xapMM:DocumentID='[^']*'",r'',a)
a = re.sub(r"(?sm)^xref\s+[0-9]+ [0-9]+(?:\s+[0-9]+ [0-9]+ [nf])*",r'',a)
a = re.sub(r"(?sm)^startxref\s+[0-9]+",r'',a)

sys.stdout.write(a)
