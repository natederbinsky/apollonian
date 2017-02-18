#!/usr/bin/env python

from __future__ import print_function

import sys
from itertools import combinations
from math import sqrt
from collections import Counter
from operator import itemgetter


def compute_curvature(A, B_c, C_c, b, include_neg):
    sum_b = sum(b)
    B = B_c * sum_b
    C = sum([x**2 for x in b]) - C_c * (sum_b**2)
    
    produced = []
    det = B**2 - 4*A*C
    Ax2 = 2*A
    if det is 0:
        produced.append(-B / Ax2)
    else:
        det_sqrt = sqrt(det)
        pos = ((-B + det_sqrt) / Ax2)
        neg = ((-B - det_sqrt) / Ax2)
        
        produced.append(pos)
        if include_neg:
            produced.append(neg)
            
    return produced

def generate(iterations, d, curvatures, debug=False):
    ret = list(curvatures)
    A = float(d-1)/float(d)
    B_c = -2.0/d
    C_c = 1.0/d
    
    if iterations>0:
        if debug: print("== Iteration 1 ==")
        first_set = compute_curvature(A, B_c, C_c, curvatures, True)
        for newbie in first_set:
            if debug: print("  {} -> {} (radius={})".format(curvatures, newbie, 1./newbie))
        last = [tuple([tuple(ret), x]) for x in first_set]
        ret += first_set
        
        for i in range(iterations-1):
            if debug: print("== Iteration {} ==".format(i+2))
            newbies = []
            for source,dest in last:
                if debug: print(" {} -> {}".format(source, dest))
                for b in combinations(source, d):
                    newbie = compute_curvature(A, B_c, C_c, [dest] + list(b), False)[0]
                    ret.append(newbie)
                    if debug: print("  {} + {} = {} (radius={})".format(dest, b, newbie, 1./newbie))
                    newbies.append(tuple([tuple([dest] + list(b)), newbie]))
            last = newbies
        if debug: print()
            
    return tuple(ret)

def usage(p):
    print("Usage: {} <iterations> <d> <r_1> ... <r_d+1>".format(p))
    sys.exit(1)

def main(argv):
    if len(argv) < 4:
        usage(argv[0])
    else:
        iterations = int(argv[1])
        d = int(argv[2])
        
        if len(argv) is not (1 + 2 + (d+1)):
            usage(argv[0])
            
        radii = tuple([1./x for x in generate(iterations, d, tuple([1./float(x) for x in argv[3:]]), True) if x > 0])
        print("Number of circles: {}".format(len(radii)))
        # print(radii)
        for k,v in sorted(Counter(radii).items(), key=itemgetter(0)):
            print("{0: >3}: {1}".format(v,k))

if __name__ == "__main__":
	main(sys.argv)
