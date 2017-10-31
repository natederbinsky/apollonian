package edu.wit.epic.apollonia;

/**
 * Class to support creation and visualization of
 * arbitrary-dimensional Apollonian spheres
 * 
 * @author derbinsky
 */
public class Element {
	
	// intentional direct
	// accessibility only
	// within the package
	final int iteration;
	final double[] x;
	double r;
	double b;
	
	// common constructor code
	private Element(int d, int iteration) {
		this.iteration = iteration;
		x = new double[d + 1];
		x[0] = 1.;
	}
	
	/**
	 * Convenience package method
	 * to generate an element via
	 * curvature without knowing
	 * coordinates
	 * 
	 * @param d dimensions
	 * @param iteration iteration of creation
	 * @param b curvature
	 */
	Element(int d, int iteration, double b) {
		this(d, iteration);
		
		this.b = b;
		this.r = 1. / b;
	}
	
	/**
	 * Creates a new element
	 * 
	 * @param d number of dimensions
	 * @param iteration iteration of creation
	 * @param r radius
	 * @param x coordinates of element
	 */
	public Element(int d, int iteration, double r, double...x) {
		this(d, iteration);
		
		this.r = r;
		this.b = (1. / r);
		
		for (int i=1; i<(x.length+1); i++) {
			this.x[i] = x[i - 1];
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(String.format("%d: %.3f/%.3f @ (%.3f", iteration, r, b, x[0]));

		for (int i=1; i<x.length; i++) {
			sb.append(String.format(", %.3f", x[i]));
		}
		sb.append(")");

		return sb.toString();
	}
	
	/**
	 * Get coordinate via dimension
	 * 
	 * @param d dimension
	 * @return coordinate
	 */
	public double getX(int d) {
		return x[d+1];
	}
	
	/**
	 * Get creation iteration
	 * 
	 * @return iteration of creation
	 */
	public int getIteration() {
		return iteration;
	}
	
	/**
	 * Get element radius
	 * 
	 * @return radius
	 */
	public double getRadius() {
		return (r<0)?(-r):(r);
	}
}
