#!/usr/bin/env python

from __future__ import print_function

import sys

def _sub(u):
    return ((u[0], u[1]), (u[0], u[2]), (u[1], u[2]))

def generate(d, iterations):
    lookup = {}
    maxlength = 1
    units = [(1,1,1),] + [(0, 1, 1) for x in range(d+1)]
    
    last = units[:]
    for x in range(iterations-1):
        newunits = []
        for unit in last:
            Node = None
            if unit in lookup:
                node = lookup[unit]
            else:
                maxlength += 1
                node = maxlength
                lookup[unit] = node
            
            for sub in _sub(unit):
                newunits.append(tuple(sorted((node, sub[0], sub[1]))))
        units += newunits
        last = newunits
        
    return units,lookup
    
def groups(units):
    counters = {}
    for u in units:
        if u in counters:
            counters[u] += 1
        else:
            counters[u] = 1
    return counters

def main(argv):
    if len(argv) < 4:
        print("Usage: {} <iterations> <r0> <x0> [x1] [x2] ...".format(argv[0]))
    else:
        iterations = int(argv[1])
        r0 = float(argv[2])
        x = [float(x) for x in argv[3:]]
        units, lookup = generate(len(x), iterations)
        
        print(len(units))
        print(units)
        print(lookup)
        print(groups(units))

if __name__ == "__main__":
	main(sys.argv)
