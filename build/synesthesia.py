#!/usr/bin/python

import re,sys

a = sys.stdin.read()

a = re.sub(r'\b(sn)(?=[123468_]|\b)',r'\\mykavtweakcolor #red \1',a)
# a = re.sub(r'\b(hh|hho|hhho|hhc)(?=[123468_]|\b)',r'\\mykavtweakcolor #yellow \1',a)
a = re.sub(r'\b(tomh)(?=[123468_]|\b)',r'\\mykavtweakcolor #yellow \1',a)
a = re.sub(r'\b(tomml)(?=[123468_]|\b)',r'\\mykavtweakcolor #blue \1',a)
a = re.sub(r'\b(cymr)(?=[123468_]|\b)',r'\\mykavtweakcolor #blue \1',a)
a = re.sub(r'\b(toml)(?=[123468_]|\b)',r'\\mykavtweakcolor #green \1',a)
a = re.sub(r'\b(cymc)(?=[123468_]|\b)',r'\\mykavtweakcolor #green \1',a)
a = re.sub(r'\b(cymcgreen)(?=[123468_]|\b)',r'\\mytweakcolor #green \1',a)
a = re.sub(r'\b(bd)(?=[123468_]|\b)',r"\\mykavtweakcolor #(x11-color 'orange) \1",a)

sys.stdout.write(a)
