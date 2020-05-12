#!/usr/bin/python

import re,sys

a = sys.stdin.read()

a = re.sub(r'\b(sn)(?=[\s123468->~()])',r'\\mytweakcolor #red \1',a)
# a = re.sub(r'\b(hh|hho|hhho|hhc)(?=[\s123468->~()])',r'\\mytweakcolor #yellow \1',a)
a = re.sub(r'\b(tomh)(?=[\s123468->~()])',r'\\mytweakcolor #yellow \1',a)
a = re.sub(r'\b(tomml)(?=[\s123468->~()])',r'\\mytweakcolor #blue \1',a)
a = re.sub(r'\b(cymr)(?=[\s123468->~()])',r'\\mytweakcolor #blue \1',a)
a = re.sub(r'\b(toml)(?=[\s123468->~()])',r'\\mytweakcolor #green \1',a)
a = re.sub(r'\b(cymc)(?=[\s123468->~()])',r'\\mytweakcolor #green \1',a)
a = re.sub(r'\b(bd)(?=[\s123468->~()])',r"\\mytweakcolor #(x11-color 'orange) \1",a)

sys.stdout.write(a)
