package edu.wit.cs.comp1050.legacy;
import java.awt.Color;

import edu.princeton.cs.introcs.Draw;

public class Test {
	
	public static void nextPosition(double[] b, double[][] y) {
		final int d = y[0].length-1;
		final double newB = b[d+1];
		
		final double A = (1 - 1./d) * newB * newB;
		
		for (int i=1; i<=d; i++) {
			double bSum = 0;
			for (int j=0; j<=d; j++) {
				bSum += (newB * b[j] * y[j][i]);
			}
			final double B = -(2./d) * bSum;
			
//			double cSum1 = 0;
//			for (int l=0; l<=d; l++) {
//				cSum1 += b[l]*b[l] * y[l][i]*y[l][i];
//			}
//			
//			double cSum2 = 0;
//			for (int j=0; j<=d; j++) {
//				for (int l=0; l<=d; l++) {
//					cSum2 += b[l]*b[j]*y[l][i]*y[j][i];
//				}
//			}
//			final double C = (cSum1 - (1./d)*cSum2) - 2;
			
			//
			
//			y[y.length-1][i] = (-B - Math.sqrt(B*B - 4*A*C) ) / (2*A);
			y[y.length-1][i] = -B / (2*A);
		}
	}

	public static void main(String[] args) {
		final double[] b = {1., 1., 1., 6.46410161514};
		final double r4 = 1/b[3];
		final double c = 1 + r4;
		
		final double[][] y = {
			{1, c, 0.},
			{1, c*Math.cos(Math.PI*2/3), c*Math.sin(Math.PI*2/3)},
			{1, c*Math.cos(Math.PI*4/3), c*Math.sin(Math.PI*4/3)},
			{1, -1000, -1000}
		};
		
		nextPosition(b, y);
		
		//
		
		Draw d = new Draw();
		d.setXscale(-3, 3);
		d.setYscale(-3, 3);
		
		d.setPenColor(Color.LIGHT_GRAY);
		d.line(-5, 0, 5, 0);
		d.line(0, -5, 0, 5);
		
		for (int i=0; i<b.length; i++) {
			d.setPenColor(Color.BLACK);
			d.circle(y[i][1], y[i][2], 1/b[i]);
			
			d.setPenColor(Color.RED);
			d.filledCircle(y[i][1], y[i][2], 0.05);
			
			System.out.printf("(%.2f, %.2f)%n", y[i][1], y[i][2]);
		}
	}

}
