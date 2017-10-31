package edu.wit.epic;

import java.awt.Color;
import java.util.Arrays;

import edu.princeton.cs.introcs.Draw;

/**
 * Application for visualizing a Fibonacci spiral
 * 
 * @author derbinsky
 */
public class FibSpiral {
	
	private static void fib(int iterations) {
		final int[] values = new int[iterations];
		values[0] = 1;
		values[1] = 1;
		
		for (int i=2; i<=(iterations-1); i++) {
			values[i] = values[i-1] + values[i-2];
		}
		
		final int[][] segments = new int[iterations][2];
		segments[0][0] = 0;
		segments[0][1] = 90;
		
		for (int i=1; i<segments.length; i++) {
			for (int j=0; j<2; j++) {
				segments[i][j] = (segments[i-1][j] + 90) % 360;
			}
		}
		
		final int[][] shifts = new int[iterations-1][2];
		shifts[0][0] = 0;
		shifts[0][1] = 0;
		
		int posneg = 0;
		int xy = 0;
		for (int i=1; i<shifts.length; i++) {
			shifts[i][xy] = ((posneg<2)?1:-1) * values[i-1];
			
			xy = (xy + 1) % 2;
			posneg = (posneg + 1) % 4;
		}
		
		final int[][] center = new int[iterations][2];
		center[0][0] = 0;
		center[0][1] = 0;
		
		for (int i=1; i<center.length; i++) {
			for (int j=0; j<2; j++) {
				center[i][j] = center[i-1][j] + shifts[i-1][j];
			}
		}
		
		System.out.println(Arrays.toString(values));
		System.out.println(Arrays.deepToString(segments));
		System.out.println(Arrays.deepToString(shifts));
		System.out.println(Arrays.deepToString(center));
		
		//
		
		final int penV = values[values.length-2];
		final int lastV = values[values.length-1];
		final int scale = (int) (0.80 * (penV + lastV));
		
		final Draw d = new Draw();
		d.setXscale(-scale, scale);
		d.setYscale(-scale, scale);
		
		d.setPenColor(Color.RED);
		for (int i=0; i<center.length; i++) {
			d.filledCircle(center[i][0], center[i][1], 0.5);
		}
		
		d.setPenColor(Color.BLUE);
		for (int i=0; i<segments.length; i++) {
			d.arc(center[i][0], center[i][1], values[i], segments[i][0], segments[i][1]);
		}
		
		d.setPenColor(Color.BLACK);
		final int[][] mult = {
			{1, 1},
			{-1, 1},
			{-1, -1},
			{1, -1},
		};
		for (int i=0; i<segments.length; i++) {
			final int seg = segments[i][0];
			final int[] m;
			if (seg == 0) {
				m = mult[0];
			} else if (seg == 90) {
				m = mult[1];
			} else if (seg == 180) {
				m = mult[2];
			} else {
				m = mult[3];
			}
			
			d.rectangle(center[i][0] + m[0]*values[i]/2., center[i][1] + m[1] * values[i]/2., values[i]/2., values[i]/2.);
		}
	}

	public static void main(String[] args) {
		// set number of iterations as argument to fib
		fib(8);
	}
}
