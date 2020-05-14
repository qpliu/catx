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

sys.stdout.write(a)
