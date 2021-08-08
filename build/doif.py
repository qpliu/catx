#!/usr/bin/env python2

import re,sys

for line in sys.stdin:
    m = re.match(r'%\s+IF\s+([^\s]+)\s(.*)$',line)
    if m and re.match(m.group(1)+'$',sys.argv[1]):
	line = m.group(2)+'\n'
    sys.stdout.write(line)
