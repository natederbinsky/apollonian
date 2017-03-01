package edu.wit.cs.comp1050;

import java.awt.Color;
import java.util.ArrayList;

import edu.princeton.cs.introcs.StdDraw3D;
import edu.wit.cs.comp1050.apollonia.Element;
import edu.wit.cs.comp1050.apollonia.Gasket;

public class Spheres {

	public static void main(String[] args) {
		
		final double min;
		final double max;
		final int TRANSPARENCY = ( (int) ( 0.5 * 255 ) );
		
		min = -5;
		max = 5;
		
		StdDraw3D.setScale( min, max );
		StdDraw3D.setInfoDisplay( false );
		StdDraw3D.setCamera( 0., 0., 1.5*max, 0., 0., 0. );
		
		//
		
		final double val = 1./Math.sqrt(2);
		final int d = 3;
		final int iterations = 6;
		
		final ArrayList<Element> elements = new ArrayList<>();
		elements.add(new Element(d, 0, 1, val, val, val));
		elements.add(new Element(d, 0, 1, -val, -val, val));
		elements.add(new Element(d, 0, 1, -val, val, -val));
		elements.add(new Element(d, 0, 1, val, -val, -val));
		
		Color colors[] = {
			Color.RED, Color.BLUE, Color.GREEN, 
			Color.ORANGE, Color.WHITE, Color.MAGENTA,
			Color.CYAN
		};
		
		Gasket.generate(d, elements, iterations);
		
		for (Element e : elements) {
			if (e.getRadius()<=elements.get(0).getRadius()) {
				StdDraw3D.setPenColor(colors[e.getIteration()], TRANSPARENCY);
				StdDraw3D.sphere(e.getX(0), e.getX(1), e.getX(2), e.getRadius());
			}
		}
		
		StdDraw3D.show();
	}

}
