package edu.wit.cs.comp1050;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.princeton.cs.introcs.Draw;

public class ApollonianSpheres {

	private static final double THRESH = 1.0E-3;
	private static final int[] MULT = {-1, 1};

	private static class Element {
		final public int iteration;
		final public double[] x;

		public double r;
		public double b;

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
	}

	private static void addElement(int iteration, List<Element> elements, List<Integer> added, int d,
			double... curvatures) {
		for (int i = 0; i < curvatures.length; i++) {
			added.add(elements.size() + i);
		}

		for (double b : curvatures) {
			elements.add(new Element(d, iteration, b));
		}
	}

	private static void computeCurvature(int d, int iteration, List<Element> elements, List<Integer> added,
			int[] indexes, double A, double B_c, double C_c, boolean includeNeg) {
		final double B;
		final double C;
		{
			double sum_b = 0;
			double sum_b2 = 0;
			for (int i = 0; i <= d; i++) {
				final double b = elements.get(indexes[i]).b;
				sum_b += b;
				sum_b2 += b * b;
			}

			B = B_c * sum_b;
			C = sum_b2 - C_c * (sum_b * sum_b);
		}

		final double disc = B * B - 4 * A * C;
		final double Ax2 = 2 * A;

		if (disc < THRESH) {
			addElement(iteration, elements, added, d, -B / Ax2);
		} else {
			final double disc_sqrt = Math.sqrt(disc);
			final double pos = ((-B + disc_sqrt) / Ax2);
			final double neg = ((-B - disc_sqrt) / Ax2);

			if (includeNeg) {
				addElement(iteration, elements, added, d, pos, neg);
			} else {
				addElement(iteration, elements, added, d, pos);
			}
		}
	}
	
	private static void _computeCoord(int[] c, double[] combo, double[][] qr) {
		for (int i=0; i<c.length; i++) {
			combo[i] = qr[i][0] + MULT[c[i]] * qr[i][1];
		}
	}

	private static void computePosition(int d, List<Element> elements, int[] indexes, double[][] qr, int[][] combos, double[] combo, double[] exp) {
		final Element nE = elements.get(indexes[d + 1]);
		final double newB = nE.b;
		final double A = (1 - 1. / d) * newB * newB;
		final double A2 = 2. * A;

		boolean singlePoint = true;
		for (int i = 1; i <= d; i++) {
			double bSum = 0;
			double cSum1 = 0;
			double cSum2 = 0;
			for (int j = 0; j <= d; j++) {
				final Element e_j = elements.get(indexes[j]);
				final double b_j = e_j.b;
				final double y_j_i = e_j.x[i];

				bSum += (newB * b_j * y_j_i);
				cSum1 += b_j * b_j * y_j_i * y_j_i;

				for (int k = 0; k <= d; k++) {
					final Element e_k = elements.get(indexes[k]);
					final double b_k = e_k.b;
					final double y_k_i = e_k.x[i];

					cSum2 += b_j * b_k * y_j_i * y_k_i;
				}
			}
			final double B = -(2. / d) * bSum;
			final double C = (cSum1 - (1. / d) * cSum2) - 2;

			qr[i - 1][0] = -B / A2;

			final double disc = B * B - 4 * A * C;
			if (disc < THRESH) {
				qr[i - 1][1] = 0.;
			} else {
				qr[i - 1][1] = Math.sqrt(disc) / A2;
				singlePoint = false;
			}
		}

		if (singlePoint) {
			for (int i = 1; i <= d; i++) {
				nE.x[i] = qr[i - 1][0];
			}
		} else {
			Double bestDiff = null;
			int[] bestC = null;
			
			for (int i=0; i<exp.length; i++) {
				final Element e = elements.get(indexes[i]);
				exp[i] = ((e.r<0)?(Math.abs(e.r)-nE.r):(e.r+nE.r));
			}
			
			for (int[] c : combos) {
				_computeCoord(c, combo, qr);
				
				double diffSum = 0.;
				for (int i=0; i<exp.length; i++) {
					final Element e = elements.get(indexes[i]);
					double sum2 = 0.;
					for (int j=0; j<c.length; j++) {
						final double cDiff = e.x[1+j] - combo[j];
						sum2 += cDiff*cDiff;
					}
					diffSum += Math.abs(Math.sqrt(sum2) - exp[i]);
				}
				
				if (bestDiff==null || diffSum<bestDiff) {
					bestDiff = diffSum;
					bestC = c;
				}
			}
			
			_computeCoord(bestC, combo, qr);
			for (int i=0; i<combo.length; i++) {
				nE.x[i+1] = combo[i];
			}
		}
	}

	private static int[] getFromPool(int d, Stack<int[]> pool) {
		if (pool.isEmpty()) {
			return new int[d + 2];
		} else {
			return pool.pop();
		}
	}

	private static void returnToPool(Stack<int[]> pool, int[] a) {
		pool.push(a);
	}

	private static void generate(int d, List<Element> elements, int iterations) {
		if (iterations > 0) {
			final Stack<int[]> indexPool = new Stack<>();
			final ArrayList<int[]> last = new ArrayList<>();
			final ArrayList<int[]> newbies = new ArrayList<>();

			final ArrayList<Integer> added = new ArrayList<>();
			final double[][] qr = new double[d][2];

			final double A = ((double) d - 1) / ((double) d);
			final double B_c = -2.0 / d;
			final double C_c = 1.0 / d;
			
			final double[] exp = new double[d+1];
			final double[] combo = new double[d];
			final int[][] combos = new int[(int) Math.pow(2, d)][d];
			for (int i=0; i<combos.length; i++) {
				final String s = String.format("%0" + d + "d", Integer.valueOf(Integer.toString(i, 2)));
				for (int j=0; j<d; j++) {
					combos[i][j] = s.charAt(j) - '0';
				}
			}

			{
				final int[] indexes = getFromPool(d, indexPool);
				for (int i = 0; i <= d; i++) {
					indexes[i] = i;
				}

				last.add(indexes);
			}

			boolean first = true;
			for (int iteration = 1; iteration <= iterations; iteration++) {
				for (int[] l : last) {
					added.clear();
					computeCurvature(d, iteration, elements, added, l, A, B_c, C_c, first);

					for (int a : added) {
						l[d + 1] = a;
						computePosition(d, elements, l, qr, combos, combo, exp);

						for (int i = 0; i <= d; i++) {
							final int[] indexes = getFromPool(d, indexPool);
							for (int j = 0; j <= d; j++) {
								if (i == j) {
									indexes[j] = a;
								} else {
									indexes[j] = l[j];
								}
							}

							newbies.add(indexes);
						}
					}
				}
				first = false;

				for (int[] l : last) {
					returnToPool(indexPool, l);
				}
				last.clear();
				last.addAll(newbies);
				newbies.clear();
			}
		}
	}
	
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
		
//		c = new double[] {1., 1., 1}; m = 1.;
//		c = new double[] {25., 25., 28.}; m = 20.;
		c = new double[] {5., 8., 8.}; m = 6.;
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
		generate(d, elements, iterations);
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
			final double r = (e.r < 0) ? -e.r : e.r;

			w.setPenColor(colors[e.iteration]);
			w.circle(e.x[1], e.x[2], r);
			
//			w.setPenColor(Color.BLACK);
//			w.text(e.x[1], e.x[2], String.format("%d", i));
			
//			System.out.println(e);
		}
		w.setPenColor(Color.BLACK);
		w.textLeft(-scale+(scale/20.), -scale+(scale/20.), String.format("Iterations: %d, Circles: %d", iterations, elements.size()));
	}

}
