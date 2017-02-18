#!/usr/bin/env python

from __future__ import print_function

import sys

# returns a tuple of tuples, each of length d via d+1 choose d
def _subcoord(d):
    return (tuple(tuple(y for y in range(d+1) if x is not y) for x in reversed(range(d+1))))
    
# returns a tuple with the values
# of t according to the indices in c[oordinates]
def _coordvals(t, c):
    return tuple(t[x] for x in c)

# returns sub-values of tuple t
# via a list of coordinates, cl
def _subvals(t, cl):
    return tuple(_coordvals(t, c) for c in cl)
    
# find u in lookup,
# if not found, add with unique value (lookup length+2)
def _node(u, lookup):
    node = lookup.get(u, None)
    if node is None:
        node = len(lookup)+2
        lookup[u] = node
        
    return node

def generate(d, iterations):
    lookup = {}
    units = [(1,)*(d+1)] + [(0,)+(1,)*(d) for x in range(d+1)]
    coordinates = _subcoord(d)
    
    last = units
    for x in range(iterations-1):
        newunits = tuple(tuple(sorted((_node(lastunit, lookup),) + subunit)) for lastunit in last for subunit in _subvals(lastunit, coordinates))
        units += newunits
        last = newunits
        
    return units,lookup
    
def groups(units):
    counters = {}
    for u in units:
        counters[u] = counters.get(u, 0) + 1
        
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
