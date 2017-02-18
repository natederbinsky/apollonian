package edu.wit.cs.comp1050;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.princeton.cs.introcs.Draw;

public class ApollonianSpheres {

	private static final double THRESH = 1.0E-3;

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

	private static void computePosition(int d, List<Element> elements, int[] indexes, double[][] qr) {
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
			// FIXME
			// Very 2D specific and slightly
			// inefficient at this point,
			// but I believe correct!

			final double xminus = qr[0][0] - qr[0][1];
			final double xplus = qr[0][0] + qr[0][1];
			final double yminus = qr[1][0] - qr[1][1];
			final double yplus = qr[1][0] + qr[1][1];
			
			final Element e0 = elements.get(indexes[0]);
			final Element e1 = elements.get(indexes[1]);
			final Element e2 = elements.get(indexes[2]);
			
			final double expected0 = ((e0.r<0)?(Math.abs(e0.r)-nE.r):(e0.r+nE.r));
			final double expected1 = ((e1.r<0)?(Math.abs(e1.r)-nE.r):(e1.r+nE.r));
			final double expected2 = ((e2.r<0)?(Math.abs(e2.r)-nE.r):(e2.r+nE.r));
			
			final double d00_0 = Math.sqrt(Math.pow(xminus-e0.x[1], 2)+Math.pow(yminus-e0.x[2], 2));
			final double d00_1 = Math.sqrt(Math.pow(xminus-e1.x[1], 2)+Math.pow(yminus-e1.x[2], 2));
			final double d00_2 = Math.sqrt(Math.pow(xminus-e2.x[1], 2)+Math.pow(yminus-e2.x[2], 2));
			final double diff00 = Math.abs(expected0-d00_0) + Math.abs(expected1-d00_1) + Math.abs(expected2-d00_2);
			
			final double d01_0 = Math.sqrt(Math.pow(xminus-e0.x[1], 2)+Math.pow(yplus-e0.x[2], 2));
			final double d01_1 = Math.sqrt(Math.pow(xminus-e1.x[1], 2)+Math.pow(yplus-e1.x[2], 2));
			final double d01_2 = Math.sqrt(Math.pow(xminus-e2.x[1], 2)+Math.pow(yplus-e2.x[2], 2));
			final double diff01 = Math.abs(expected0-d01_0) + Math.abs(expected1-d01_1) + Math.abs(expected2-d01_2);
			
			final double d10_0 = Math.sqrt(Math.pow(xplus-e0.x[1], 2)+Math.pow(yminus-e0.x[2], 2));
			final double d10_1 = Math.sqrt(Math.pow(xplus-e1.x[1], 2)+Math.pow(yminus-e1.x[2], 2));
			final double d10_2 = Math.sqrt(Math.pow(xplus-e2.x[1], 2)+Math.pow(yminus-e2.x[2], 2));
			final double diff10 = Math.abs(expected0-d10_0) + Math.abs(expected1-d10_1) + Math.abs(expected2-d10_2);
			
			final double d11_0 = Math.sqrt(Math.pow(xplus-e0.x[1], 2)+Math.pow(yplus-e0.x[2], 2));
			final double d11_1 = Math.sqrt(Math.pow(xplus-e1.x[1], 2)+Math.pow(yplus-e1.x[2], 2));
			final double d11_2 = Math.sqrt(Math.pow(xplus-e2.x[1], 2)+Math.pow(yplus-e2.x[2], 2));
			final double diff11 = Math.abs(expected0-d11_0) + Math.abs(expected1-d11_1) + Math.abs(expected2-d11_2);
			
			final double x;
			final double y;
			if (diff00<diff01 && diff00<diff10 && diff00<diff11) {
				x = xminus;
				y = yminus;
			} else if (diff01<diff10 && diff01<diff11) {
				x = xminus;
				y = yplus;
			} else if (diff10<diff11) {
				x = xplus;
				y = yminus;
			} else {
				x = xplus;
				y = yplus;
			}
			
			nE.x[1] = x;
			nE.x[2] = y;
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
						computePosition(d, elements, l, qr);

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

	public static void main(String[] args) {
		final int iterations = 4;
		final int d = 2;

		final int scale = 3;

		final ArrayList<Element> elements = new ArrayList<>();
		elements.add(new Element(d, 0, 1., 1.15470, 0.));
		elements.add(new Element(d, 0, 1., -0.57735, -1.));
		elements.add(new Element(d, 0, 1., -0.57735, 1.));

		//

		generate(d, elements, iterations);

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
			Color.MAGENTA, Color.YELLOW, Color.CYAN 
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
		w.text(-(2*scale/3.), -scale+(scale/20.), String.format("Iterations: %d, Circles: %d", iterations, elements.size()));
	}

}
