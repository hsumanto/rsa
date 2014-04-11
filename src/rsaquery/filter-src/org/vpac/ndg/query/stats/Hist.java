package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Groups values together based on their intrinsic distribution.
 *
 * @author Alex Fraser
 */
public class Hist implements Foldable<Hist> {

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	public ScalarElement prototype;

	private double[] lbs;
	private Stats[] buckets;
	private int mruBucket;

	public Hist(ScalarElement prototype) {
		this.prototype = prototype;
		lbs = genBuckets(BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE,
				NUM_BUCKETS);

		buckets = new Stats[lbs.length];
		mruBucket = NUM_BUCKETS / 2;
		for (int i = 0; i < lbs.length; i++) {
				buckets[i] = new Stats(prototype);
		}
	}

	public void update(ScalarElement value) {
		Stats stats;
		if (value.compareTo(lbs[mruBucket]) >= 0 &&
				value.compareTo(lbs[mruBucket + 1]) < 0) {
			stats = buckets[mruBucket];
		} else {
			mruBucket = Arrays.binarySearch(lbs, value.doubleValue());
			if (mruBucket < 0)
				mruBucket = (0 - mruBucket) - 2;
			stats = buckets[mruBucket];
		}
		stats.update(value);
	}

	@Override
	public Hist fold(Hist other) {
		Hist res = new Hist(prototype);

		for (int i = 0; i < lbs.length; i++) {
			res.buckets[i] = res.buckets[i].fold(other.buckets[i]);
		}

		return res;
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
		buckets.add(Double.POSITIVE_INFINITY);

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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");

		boolean firstBucket = true;
		for (int i = 0; i < lbs.length; i++) {
			Stats stats = buckets[i];
			if (stats.n <= 0)
				continue;

			if (!firstBucket)
				sb.append(", ");
			else
				firstBucket = false;

			sb.append(String.format("{%g: %d}", lbs[i], stats.n));
		}
		sb.append("]");

		return sb.toString();
	}

	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		sb.append("LowerBound,count,min,max,mean,stddev\n");
		for (int i = 0; i < lbs.length; i++) {
			Stats stats = buckets[i];
			if (stats.getCount().longValue() == 0)
				continue;
			sb.append(String.format("%g,%d,%g,%g,%g,%g\n", lbs[i], stats.n,
					stats.getMin().doubleValue(), stats.getMax().doubleValue(),
					stats.getMean().doubleValue(),
					stats.getStdDev().doubleValue()));
		}
		return sb.toString();
	}
}
