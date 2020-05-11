#!/usr/bin/python

import re,sys

a = sys.stdin.read()

a = re.sub(r'^drsPart =',r'drscolorPart =',a)

a = re.sub(r'\b(sn)(?=[\s123468->~()])',r'\\tweak color #red \1',a)
# a = re.sub(r'\b(hh|hho|hhho|hhc)(?=[\s123468->~()])',r'\\tweak color #yellow \1',a)
a = re.sub(r'\b(tomh)(?=[\s123468->~()])',r'\\tweak color #yellow \1',a)
a = re.sub(r'\b(tomml)(?=[\s123468->~()])',r'\\tweak color #blue \1',a)
a = re.sub(r'\b(cymr)(?=[\s123468->~()])',r'\\tweak color #blue \1',a)
a = re.sub(r'\b(toml)(?=[\s123468->~()])',r'\\tweak color #green \1',a)
a = re.sub(r'\b(cymc)(?=[\s123468->~()])',r'\\tweak color #green \1',a)
a = re.sub(r'\b(bd)(?=[\s123468->~()])',r"\\tweak color #(x11-color 'orange) \1",a)

sys.stdout.write(a)
