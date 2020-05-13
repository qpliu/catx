#!/usr/bin/env python

import sys

for a in sys.stdin:
    if a.startswith('/ID [') and a.endswith(']\n'):
	continue
    if a.startswith('/CreationDate(') and a.endswith(')\n'):
	continue
    if a.startswith('/ModDate(') and a.endswith(')\n'):
	continue
    if a.startswith('<rdf:Description') and a.endswith('>\n'):
	continue
    if a.startswith('<xmp:CreateDate') and a.endswith('>\n'):
	continue
    sys.stdout.write(a)
