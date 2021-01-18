#!/usr/bin/python

import re,sys

a = sys.stdin.read()

a = re.sub(r'\b(sn)(?=[^a-z]|\b)',r'\\mykavtweakcolor #red \1',a)
# a = re.sub(r'\b(hh|hho|hhho|hhc)(?=[^a-z]|\b)',r"\\mykavtweakcolor #(x11-color 'yellow2) \1",a)
a = re.sub(r'\b(tomh)(?=[^a-z]|\b)',r"\\mykavtweakcolor #(x11-color 'yellow3) \1",a)
a = re.sub(r'\b(tomml)(?=[^a-z]|\b)',r'\\mykavtweakcolor #blue \1',a)
a = re.sub(r'\b(cymr)(?=[^a-z]|\b)',r'\\mykavtweakcolor #blue \1',a)
a = re.sub(r'\b(toml)(?=[^a-z]|\b)',r'\\mykavtweakcolor #green \1',a)
a = re.sub(r'\b(cymc)(?=[^a-z]|\b)',r'\\mykavtweakcolor #green \1',a)
a = re.sub(r'\b(bd)(?=[^a-z]|\b)',r"\\mykavtweakcolor #(x11-color 'orange) \1",a)

a = re.sub(r'\b([a-z]+)(yellow)(?=[^a-z]|\b)',r"\\mytweakcolor #(x11-color '\g<2>3) \1",a)
a = re.sub(r'\b([a-z]+)(red|green|blue|orange)(?=[^a-z]|\b)',r"\\mytweakcolor #(x11-color '\2) \1",a)

sys.stdout.write(a)
