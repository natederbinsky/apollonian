package edu.wit.epic.apollonia;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Dimension-agnostic library for
 * generating Apollonian spheres
 * 
 * @author derbinsky
 */
public class Gasket {
	private static final double THRESH = 1.0E-10;
	private static final int[] MULT = { -1, 1 };

	private static void addElement(int iteration, List<Element> elements, List<Integer> added, int d, double...curvatures) {
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
		for (int i = 0; i < c.length; i++) {
			combo[i] = qr[i][0] + MULT[c[i]] * qr[i][1];
		}
	}

	private static void computePosition(int d, List<Element> elements, int[] indexes, double[][] qr, int[][] combos,
			double[] combo, double[] combo2, double[] exp) {
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

			double expSum = 0.;
			for (int i = 0; i < exp.length; i++) {
				final Element e = elements.get(indexes[i]);
				
				final double expV;
				if (e.r < 0) {
					expV = Math.abs(e.r) - nE.r;
				} else if (nE.r < 0) {
					expV = Math.abs(nE.r) - e.r;
				} else {
					expV = e.r + nE.r;
				}
				exp[i] = expV;
				expSum += expV;
			}

			for (int[] c : combos) {
				_computeCoord(c, combo, qr);

				double diffSum = 0.;
				for (int i = 0; i < exp.length; i++) {
					final Element e = elements.get(indexes[i]);
					double sum2 = 0.;
					for (int j = 0; j < c.length; j++) {
						final double cDiff = e.x[1 + j] - combo[j];
						sum2 += cDiff * cDiff;
					}
					diffSum += Math.abs(Math.sqrt(sum2) - exp[i]);
				}
				
				if (bestDiff == null || diffSum < bestDiff) {
					
					// rare edge case to break symmetries
					boolean go = true;
					if ((bestDiff!= null) && (diffSum/expSum<THRESH && bestDiff/expSum<THRESH)) {
						_computeCoord(bestC, combo2, qr);
						
						final double pDistance;
						{
							double pSum = 0.;
							for (int i = 0; i < c.length; i++) {
								final double pDiff = combo[i] - combo2[i];
								pSum += pDiff*pDiff;
							}
							pDistance = Math.sqrt(pSum);
						}
								
						if (pDistance>THRESH) {
							for (Element e : elements) {
								double eSum = 0.;
								for (int i = 0; i < c.length; i++) {
									final double eDiff = e.x[1+i] - combo[i];
									eSum += eDiff*eDiff;
								}
								final double eDistance = Math.sqrt(eSum);
								if (eDistance < THRESH) {
									go = false;
								}
							}
						} else {
							go = false;
						}
					}
					
					if (go) {
						bestDiff = diffSum;
						bestC = c;
					}
				}
			}

			_computeCoord(bestC, combo, qr);
			for (int i = 0; i < combo.length; i++) {
				nE.x[i + 1] = combo[i];
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

	/**
	 * Generates an Apollonian sphere
	 * in a specified dimension for
	 * a specified number of iterations
	 * 
	 * @param d dimensions
	 * @param elements destination list (includes seed elements)
	 * @param iterations number of iterations for which to generate
	 */
	public static void generate(int d, List<Element> elements, int iterations) {
		if (iterations > 0) {
			final Stack<int[]> indexPool = new Stack<>();
			final ArrayList<int[]> last = new ArrayList<>();
			final ArrayList<int[]> newbies = new ArrayList<>();

			final ArrayList<Integer> added = new ArrayList<>();
			final double[][] qr = new double[d][2];

			final double A = ((double) d - 1) / ((double) d);
			final double B_c = -2.0 / d;
			final double C_c = 1.0 / d;

			final double[] exp = new double[d + 1];
			final double[] combo = new double[d];
			final double[] combo2 = new double[d];
			final int[][] combos = new int[(int) Math.pow(2, d)][d];
			for (int i = 0; i < combos.length; i++) {
				final String s = String.format("%0" + d + "d", Integer.valueOf(Integer.toString(i, 2)));
				for (int j = 0; j < d; j++) {
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
						computePosition(d, elements, l, qr, combos, combo, combo2, exp);

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
}
