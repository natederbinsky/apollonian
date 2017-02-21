package edu.wit.cs.comp1050.apollonia;

public class Element {
	final int iteration;
	final double[] x;

	double r;
	double b;

	public Element(int d, int iteration) {
		this.iteration = iteration;
		x = new double[d + 1];
		x[0] = 1.;
	}

	public Element(int d, int iteration, double b) {
		this(d, iteration);
		this.b = b;
		this.r = 1. / b;
	}

	public Element(int d, int iteration, double r, double... x) {
		this(d, iteration, 1. / r);
		for (int i = 1; i < this.x.length; i++) {
			this.x[i] = x[i - 1];
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(String.format("%d: %.3f/%.3f @ (%.3f", iteration, r, b, x[0]));

		for (int i = 1; i < x.length; i++) {
			sb.append(String.format(", %.3f", x[i]));
		}
		sb.append(")");

		return sb.toString();
	}
	
	public double getX(int d) {
		return x[d+1];
	}
	
	public int getIteration() {
		return iteration;
	}
	
	public double getRadius() {
		return (r<0)?(-r):(r);
	}
}
