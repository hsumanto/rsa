package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.vpac.ndg.query.iteration.Pair;

public class MockData {

	/**
	 * @param n The number of sides on the first die.
	 * @param m The number of sides on the second die.
	 * @return A list of permutations for the rolling and summing of two dice.
	 *         This should result in a perfect normal distribution.
	 */
	public static List<Integer> permute(int n, int m) {
		List<Integer> permutations = new ArrayList<Integer>();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				permutations.add(i + j);
			}
		}
		return permutations;
	}

	public static class ParallelRand {
		Random generator;
		List<double[]> bounds;
		public ParallelRand(List<double[]> bounds) {
			generator = new Random(0L);
			for (double[] b : bounds) {
				if (b.length != 2) {
					throw new IllegalArgumentException(
						"Bounds must be in pairs");
				}
			}
			this.bounds = bounds;
		}

		public List<Double> nextDoubles() {
			List<Double> res = new ArrayList<>(bounds.size());
			for (int i = 0; i < bounds.size(); i++) {
				double[] b = bounds.get(i);
				res.add(lerp(b[0], b[1], generator.nextDouble()));
			}
			return res;
		}

		private double lerp(double a, double b, double fac) {
			return ((b - a) * fac) + a;
		}
	}

	/**
	 * @param n The number of sides on the first die.
	 * @param m The number of sides on the second die.
	 * @return A list of permutations for the rolling of two dice. This should
	 *         result in a perfect normal distribution.
	 */
	public static List<Pair<Integer, Integer>> permutePairs(int n, int m) {
		List<Pair<Integer, Integer>> permutations = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				Pair<Integer, Integer> p = new Pair<Integer, Integer>();
				p.a = i;
				p.b = j;
				permutations.add(p);
			}
		}
		return permutations;
	}

	public static double mean(List<Integer> xs) {
		long m = 0;
		for (Integer x : xs) {
			m += x;
		}
		return (double)m / xs.size();
	}

	/**
	 * Find the standard deviation of a list of integers. This uses a fool-proof
	 * two-pass algorithm.
	 * http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Two-pass_algorithm
	 */
	public static double stddev(List<Integer> xs) {
		double m = mean(xs);
		double variance = 0.0;
		for (Integer x : xs) {
			variance += (x - m) * (x - m);
		}
		variance /= xs.size() - 1;
		return Math.sqrt(variance);
	}

}
