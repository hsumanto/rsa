package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.Element;

/**
 * Groups values together.
 * 
 * @author Alex Fraser
 */
public class Hist implements Foldable<Hist> {

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	public Element<?> min;

	private double[] lbs;

	public Hist(Element<?> prototype) {
		lbs = genBuckets(BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE,
				NUM_BUCKETS);
	}

	/**
	 * Generates a symmetric array of buckets, with zero in the middle. The
	 * first bucket should catch all very large negative numbers, while the last
	 * bucket catches all very large positive numbers. Therefore the first
	 * bucket has a bound of negative infinity.
	 *
	 * @param base
	 *            The base order.
	 * @param root
	 *            The number of buckets per order of magnitude.
	 * @param scale
	 *            The scaling factor.
	 * @param n
	 *            The number of buckets to generate.
	 * @return An array of bucket lower bounds.
	 */
	static double[] genBuckets(double base, double root, double scale, int n) {
		List<Double> buckets = new ArrayList<Double>(n);

		// Lower bound generation only works for positive numbers! So do this
		// in three steps:
		// 1. Compute lower bounds of positive numbers and add to list.
		// 2. Add 0.0.
		// 3. Repeat 1, but negate numbers before insertion.
		// 4. Add NEGATIVE_INFINITY, to catch all very large negative values.
		// 4. Sort the list.

		for (int i = 0; i < n / 2; i++)
			buckets.add(lowerBound(i, base, root, scale));

		buckets.add(0.0);

		for (int i = 0; i < (n / 2) - 1; i++)
			buckets.add(0.0 - lowerBound(i, base, root, scale));

		buckets.add(Double.NEGATIVE_INFINITY);

		Collections.sort(buckets);

		double[] bs = new double[buckets.size()];
		for (int i = 0; i < bs.length; i++)
			bs[i] = buckets.get(i);

		return bs;
	}

	/**
	 * Computes the lower bound of a bucket. This only generates lower bounds
	 * for positive indices.
	 *
	 * @param i
	 *            The index to generate a lower bound of.
	 * @param base
	 *            The base order.
	 * @param root
	 *            The number of buckets per order of magnitude.
	 * @param scale
	 *            The scaling factor.
	 * @return The lower bound of the bucket.
	 */
	static double lowerBound(int i, double base, double root, double scale) {
		return Math.pow(base, i / root) * scale;
	}

	public void update(Element<?> value) {
		// TODO
	}

	@Override
	public Hist fold(Hist other) {
		Hist res = new Hist(min);

		// TODO
		return res;
	}

	@Override
	public String toString() {
		// TODO
		return null;
	}

}