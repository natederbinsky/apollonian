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
			// Very much a 2D hack at this point
			// appears to work for many cases
			// but inefficient and not quite right

			final double xminus = qr[0][0] - qr[0][1];
			final double xplus = qr[0][0] + qr[0][1];
			final double yminus = qr[1][0] - qr[1][1];
			final double yplus = qr[1][0] + qr[1][1];

			final int[] counts = new int[4];

			for (Element e : elements) {
				if (e == nE)
					continue;

				final double rSum;
				if (e.r < 0) {
					rSum = Math.abs(e.r) - nE.r;
				} else {
					rSum = e.r + nE.r;
				}

				final double d00 = Math.sqrt(Math.pow(e.x[1] - xminus, 2) + Math.pow(e.x[2] - yminus, 2));
				final double d01 = Math.sqrt(Math.pow(e.x[1] - xminus, 2) + Math.pow(e.x[2] - yplus, 2));
				final double d10 = Math.sqrt(Math.pow(e.x[1] - xplus, 2) + Math.pow(e.x[2] - yminus, 2));
				final double d11 = Math.sqrt(Math.pow(e.x[1] - xplus, 2) + Math.pow(e.x[2] - yplus, 2));

				final double diff00 = (d00 + THRESH) - rSum;
				final double diff01 = (d01 + THRESH) - rSum;
				final double diff10 = (d10 + THRESH) - rSum;
				final double diff11 = (d11 + THRESH) - rSum;

				if (diff00 > 0)
					counts[0]++;
				if (diff01 > 0)
					counts[1]++;
				if (diff10 > 0)
					counts[2]++;
				if (diff11 > 0)
					counts[3]++;
			}

			final int total = elements.size() - 2;
			if (counts[0] >= total) {
				nE.x[1] = xminus;
				nE.x[2] = yminus;
			} else if (counts[1] >= total) {
				nE.x[1] = xminus;
				nE.x[2] = yplus;
			} else if (counts[2] >= total) {
				nE.x[1] = xplus;
				nE.x[2] = yminus;
			} else if (counts[3] >= total) {
				nE.x[1] = xplus;
				nE.x[2] = yplus;
			}

			// System.out.printf("boo: %s%n", Arrays.toString(counts));
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
		final int iterations = 3;
		final int d = 2;

		final int scale = 3;

		final ArrayList<Element> elements = new ArrayList<>();
		elements.add(new Element(d, 0, 1., 1.15470, 0.));
		elements.add(new Element(d, 0, 1., -0.57735, -1.));
		elements.add(new Element(d, 0, 1., -0.57735, 1.));

		//

		generate(d, elements, iterations);

		//

		final Draw w = new Draw();
		w.setCanvasSize(900, 900);
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

		final Color[] colors = { Color.BLACK, Color.ORANGE, Color.BLUE, Color.GRAY, Color.RED, Color.GREEN,
				Color.MAGENTA, Color.YELLOW, Color.CYAN };

		for (Element e : elements) {
			final double r = (e.r < 0) ? -e.r : e.r;

			w.setPenColor(colors[e.iteration]);

			w.circle(e.x[1], e.x[2], r);
			System.out.println(e);
		}
	}

}
