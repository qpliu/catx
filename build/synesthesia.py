#!/usr/bin/python2

import re,sys

a = sys.stdin.read()

a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(sn)(?=[^a-z]|\b)',r' \\mykavtweakcolor #red \1',a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(tomh)(?=[^a-z]|\b)',r" \\mykavtweakcolor #(x11-color 'yellow3) \1",a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(tomml)(?=[^a-z]|\b)',r' \\mykavtweakcolor #blue \1',a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(cymr)(?=[^a-z]|\b)',r' \\mykavtweakcolor #blue \1',a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(toml)(?=[^a-z]|\b)',r' \\mykavtweakcolor #green \1',a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(cymc)(?=[^a-z]|\b)',r' \\mykavtweakcolor #green \1',a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)(bd)(?=[^a-z]|\b)',r" \\mykavtweakcolor #(x11-color 'orange) \1",a)

a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)([a-z]+)(yellow)(?=[^a-z]|\b)',r" \\mytweakcolor #(x11-color '\g<2>3) \1",a)
a = re.sub(r'(?:(?<=[<{])\s*|(?<!\\parenthesize)(?<!\s)\s+)([a-z]+)(red|green|blue|orange)(?=[^a-z]|\b)',r" \\mytweakcolor #(x11-color '\2) \1",a)
a = re.sub(r'\\parenthesize\b',r'\\parenthesize\\mytweakcolor "#d0d0d0"',a)

sys.stdout.write(a)
