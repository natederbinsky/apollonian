package edu.wit.cs.comp1050.legacy;
import java.awt.Color;

import edu.princeton.cs.introcs.Draw;

public class Test2 {
	
	public static double centroidx;
	public static double centroidy;
	
	public static double basex;
	public static double basey;
	
	public static double[][] nextPosition(Draw draw, double[] b, double[][] y) {
		final int d = y[0].length-1;
		final double newB = b[d+1];
		
		final double A = (1 - 1./d) * newB * newB;
		
		double[][] ret = new double[d][2];
		
		System.out.printf("Centroid: (%.3f, %.3f)%n", 1./3.*(y[0][1]+y[1][1]), 1./3.*(y[0][2]+y[1][2]));
		centroidx = 1./3.*(y[0][1]+y[1][1]);
		centroidy = 1./3.*(y[0][2]+y[1][2]);
		
		for (int i=1; i<=d; i++) {
			double bSum = 0;
			for (int j=0; j<=d; j++) {
				bSum += (newB * b[j] * y[j][i]);
			}
			final double B = -(2./d) * bSum;
			
			double cSum1 = 0;
			for (int l=0; l<=d; l++) {
				cSum1 += b[l]*b[l] * y[l][i]*y[l][i];
			}
			
			double cSum2 = 0;
			for (int j=0; j<=d; j++) {
				for (int l=0; l<=d; l++) {
					cSum2 += b[l]*b[j]*y[l][i]*y[j][i];
				}
			}
			final double C = (cSum1 - (1./d)*cSum2) - 2;
			
			//
			
			final int mult = (i==1)?1:-1;
			
			System.out.printf("%s: %.3f%n", i==1?"x":"y", -B/(2*A));
			if (i==1)
				basex = -B/(2*A);
			else
				basey = -B/(2*A);
			
			ret[i-1][0] = (-B + Math.sqrt(B*B - 4*A*C) ) / (2*A);
			ret[i-1][1] = (-B - Math.sqrt(B*B - 4*A*C) ) / (2*A);
			
			y[y.length-1][i] = (-B + mult * Math.sqrt(B*B - 4*A*C) ) / (2*A);
		}
		
		return ret;
	}
	
	private static void circ(Draw d, double x, double y, double r) {
		d.setPenColor(Color.BLACK);
		d.circle(x, y, r);
		
		d.setPenColor(Color.RED);
		d.filledCircle(x, y, r/15.);
	}

	public static void main(String[] args) {
		final double[] b = {1., 1., 6.46410161514, 15.9282032303};
		final double r4 = 1/b[2];
		final double c = 1 + r4;
		
		final double[][] y = {
			{1, c*Math.cos(Math.PI*2/3), c*Math.sin(Math.PI*2/3)},
			{1, c*Math.cos(Math.PI*4/3), c*Math.sin(Math.PI*4/3)},
			{1, 0.0, 0.0},
			{1, -100, -100}
		};
		
		Draw d = new Draw();
		
		//
		
		double[][] r = nextPosition(d, b, y);
		
		//
		
		
		d.setXscale(-1.5, 1.5);
		d.setYscale(-1.5, 1.5);
		
		d.setPenColor(Color.LIGHT_GRAY);
		d.line(-5, 0, 5, 0);
		d.line(0, -5, 0, 5);
		
		for (int i=0; i<b.length-1; i++) {
			circ(d, y[i][1], y[i][2], 1/b[i]);
		}
		
		int[][] coords = { {0, 0}, {0, 1}, {1, 0}, {1, 1} };
		for (int[] coord : coords) {
			final double cx = r[0][coord[0]];
			final double cy = r[1][coord[1]];
			
			circ(d, cx, cy, 1/b[b.length-1]);
			
			System.out.printf("%.3f %.3f%n", cx, cy);
		}
		
		d.setPenColor(Color.GREEN);
		d.filledCircle(centroidx, centroidy, 0.05);
		
		d.setPenColor(Color.ORANGE);
		d.filledCircle(basex, basey, 0.05);
	}

}
