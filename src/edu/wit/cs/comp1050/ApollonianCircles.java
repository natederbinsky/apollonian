package edu.wit.cs.comp1050;

import java.awt.Color;
import java.util.ArrayList;

import edu.princeton.cs.introcs.Draw;
import edu.wit.cs.comp1050.apollonia.Element;
import edu.wit.cs.comp1050.apollonia.Gasket;

public class ApollonianCircles {
	
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
		final int iterations = 6;
		final int d = 2;

		final int scale = 3;
		
		//
		
		final double m;
		final double[] c;
		
		c = new double[] {1., 1., 1}; m = 1.;
//		c = new double[] {25., 25., 28.}; m = 20.;
//		c = new double[] {5., 8., 8.}; m = 6.;
//		c = new double[] {10., 15., 19.}; m = 6.;
//		c = new double[] {23., 27., 18.}; m = 16.;
//		c = new double[] {2., 2., 3.}; m = 1.; // interesting... negative flip?
		
		final double r1 = m*1./c[0];
		final double r2 = m*1./c[1];
		final double r3 = m*1./c[2];
		
		final double x1 = 0.;
		final double y1 = 0.;
		final double x2 = r1 + r2;
		final double y2 = 0.;
		
		final double[] xy = getXY(r1, r2, r3);
		final double[] o = getOffset(x1, y1, x2, y2, xy[0], xy[1]);

		final ArrayList<Element> elements = new ArrayList<>();
		elements.add(new Element(d, 0, r1, o[0] + x1, o[1] + y1));
		elements.add(new Element(d, 0, r2, o[0] + x2, o[1] + y2));
		elements.add(new Element(d, 0, r3, o[0] + xy[0], o[1] + xy[1]));

		//

		System.out.print("Generating... ");
		Gasket.generate(d, elements, iterations);
		System.out.println("done.");

		//

		final Draw w = new Draw("Apollonia!");
		w.setCanvasSize(600, 600);
		w.setXscale(-scale, scale);
		w.setYscale(-scale, scale);
		w.clear(Color.WHITE);

		w.setPenColor(Color.LIGHT_GRAY);
		w.line(-scale, 0, scale, 0);
		w.line(0, -scale, 0, scale);

		for (int i = (-scale + 1); i < scale; i++) {
			w.line(i, -scale / 100., i, scale / 100.);
			w.line(-scale / 100., i, scale / 100., i);
		}

		//

		final Color[] colors = { 
			Color.BLACK, Color.ORANGE, Color.BLUE, 
			Color.GRAY, Color.RED, Color.GREEN,
			Color.MAGENTA, Color.YELLOW, Color.CYAN,
			Color.PINK,
		};

		for (int i=0; i<elements.size(); i++) {
			final Element e = elements.get(i);

			w.setPenColor(colors[e.getIteration()]);
			w.circle(e.getX(0), e.getX(1), e.getRadius());
			
//			w.setPenColor(Color.BLACK);
//			w.text(e.x[1], e.x[2], String.format("%d", i));
			
//			System.out.println(e);
		}
		w.setPenColor(Color.BLACK);
		w.textLeft(-scale+(scale/20.), -scale+(scale/20.), String.format("Iterations: %d, Circles: %d", iterations, elements.size()));
	}

}
