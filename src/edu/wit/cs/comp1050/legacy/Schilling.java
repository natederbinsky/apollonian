package edu.wit.cs.comp1050.legacy;

import java.util.Arrays;

public class Schilling {
	
	private static double[] getXY(double r1, double r2, double r3) {
		final double a = r2 + r3;
		final double b = r1 + r3;
		final double c = r1 + r2;
		
		final double x = (b*b + c*c - a*a) / (2 * c);
		final double y = Math.sqrt(b*b - x*x);
		
		return new double[] {x, y};
	}
	
	private static double[] getOffset(double x1, double y1, double x2, double y2, double x3, double y3) {
		final double cx = 1./3.*(x1 + x2 + x3);
		final double cy = 1./3.*(y1 + y2 + y3);
		
		return new double[] {-cx, -cy};
	}

	public static void main(String[] args) {
		final double[] xy = getXY(1, 1, 1);
		System.out.println(Arrays.toString(getOffset(0., 0., 2., 0., xy[0], xy[1])));
	}

}
