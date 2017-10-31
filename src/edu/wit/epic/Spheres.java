package edu.wit.epic;

import java.awt.Color;
import java.util.ArrayList;

import edu.princeton.cs.introcs.StdDraw3D;
import edu.wit.epic.apollonia.Element;
import edu.wit.epic.apollonia.Gasket;

/**
 * Application for visualizing
 * Apollonian spheres
 * 
 * @author derbinsky
 */
public class Spheres {
	
	private static void goSpheres(Color[] colors) {
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
		
		final ArrayList<Element> elements = new ArrayList<>();
		elements.add(new Element(d, 0, 1, val, val, val));
		elements.add(new Element(d, 0, 1, -val, -val, val));
		elements.add(new Element(d, 0, 1, -val, val, -val));
		elements.add(new Element(d, 0, 1, val, -val, -val));
		
		Gasket.generate(d, elements, colors.length-1);
		
		for (Element e : elements) {
			System.out.println(String.format("apollonian(%.2f, %.2f, %.2f, %.2f, %d, %d, %d);", e.getX(0), e.getX(1), e.getX(2), e.getRadius(), colors[e.getIteration()].getRed(), colors[e.getIteration()].getGreen(), colors[e.getIteration()].getBlue()));
			
			if (e.getRadius()<=elements.get(0).getRadius()) {
				StdDraw3D.setPenColor(colors[e.getIteration()], TRANSPARENCY);
				StdDraw3D.sphere(e.getX(0), e.getX(1), e.getX(2), e.getRadius());
			}
		}
		
		StdDraw3D.show();
	}

	public static void main(String[] args) {
		// control number of iterations via
		// colors (iterations = colors.length-1)
		final Color colors[] = {
			Color.RED, Color.BLUE, Color.GREEN, 
			Color.ORANGE, Color.WHITE, Color.MAGENTA,
			Color.CYAN
		};
		
		goSpheres(colors);
	}

}
