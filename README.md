# Virtual Fractality

Library and example applications to visualize various fractals.

## Installation

Required software:
* JDK8 (might work with later, untested)
* Eclipse (`.project` provided, could be changed to other IDEs or command line)
* OpenSCAD (for producing STL of spheres)

Simply import the base folder into Eclipse as a project.
Applications should just run (each has customization options in main).

The Spheres application outputs to terminal calls that can be copy-pasted into the scad source (`apollonia` module).
Then simply render and export as STL (or other format of your choice).

## Applications

### FibSpiral
Visualizes a Fibonacci spiral up to `k` iterations.

### CirclesFX
Produces an Apollonian gasket given initial curvatures and iterations.

### Spheres
Visualizes Apollonian spheres given iterations.
Also produces coordinates/radii that can be easily used with OpenSCAD for STL export.

### OpenSCAD
In the `scripts` folder the `apollonian.scad` file provides an easy way to export spheres to STL (or other formats).
Simply take the output of the Spheres application, paste into the `apollonia` module.
The default operation is to then perform a a cut along a plane, but this can be easily commented out/modified.
