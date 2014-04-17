package org.vpac.ndg.query.stats;

import java.io.Serializable;
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
public class Hist implements Foldable<Hist>, Serializable {

	// Beware! If you change these, old histograms will not be comparable to
	// new ones.
	private static final long serialVersionUID = 1L;
	private String id;
	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	public ScalarElement prototype;
	private double[] lowerBounds;
	private List<Bucket> buckets;
	private Bucket mruBucket;

	public Hist(ScalarElement prototype) {
		this.prototype = prototype;
		lowerBounds = genBuckets(BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE,
				NUM_BUCKETS);

		buckets = new ArrayList<Bucket>();
		for (int i = 0; i < lowerBounds.length - 1; i++) {
			buckets.add(new Bucket(lowerBounds[i], lowerBounds[i + 1],
					new Stats()));
		}

		mruBucket = buckets.get(buckets.size() / 2);
	}

	public void update(ScalarElement value) {
		if (!value.isValid())
			return;
		if (!mruBucket.canContain(value)) {
			int i = Arrays.binarySearch(lowerBounds, value.doubleValue());
			if (i < 0)
				i = (0 - i) - 2;
			mruBucket = buckets.get(i);
		}
		mruBucket.getStats().update(value);
	}

	@Override
	public Hist fold(Hist other) {
		Hist res = new Hist(prototype);

		for (int i = 0; i < buckets.size(); i++) {
			res.buckets.set(i, buckets.get(i).fold(other.buckets.get(i)));
		}

		return res;
	}

	public List<Bucket> getBuckets() {
		return buckets;
	}

	public List<Bucket> getNonemptyBuckets() {
		List<Bucket> bs = new ArrayList<Bucket>();
		for (Bucket b : buckets) {
			if (b.getStats().getN() > 0)
				bs.add(b);
		}
		return bs;
	}

	/**
	 * @return A single stats object that summarises all values in this
	 *         histogram.
	 */
	public Stats getSummary() {
		Stats res = new Stats();
		for (Bucket b : buckets) {
			if (b.getStats().getN() > 0)
				res = res.fold(b.getStats());
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

		// Lower bound generation only works for positive numbers! So do this
		// in three steps:
		// 1. Compute lower bounds of positive numbers and add to list.
		// 2. Add 0.0.
		// 3. Repeat 1, but negate numbers before insertion.
		// 4. Add NEGATIVE_INFINITY, to catch all very large negative values.
		// 4. Sort the list.

		List<Double> buckets = new ArrayList<Double>();

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
		for (int i = 0; i < buckets.size(); i++) {
			Bucket b = buckets.get(i);

			if (b.getStats().getN() <= 0)
				continue;

			if (!firstBucket)
				sb.append(", ");
			else
				firstBucket = false;

			sb.append(String.format("%d: {%g-%g: n=%d, mean=%g}", i,
					b.getLower(), b.getUpper(),
					b.getStats().getCount(),
					b.getStats().getMean()));
		}
		sb.append("]");

		return sb.toString();
	}

	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		sb.append("LowerBound,count,min,max,mean,stddev\n");
		for (Bucket b : buckets) {
			Stats stats = b.getStats();
			if (stats.getCount() == 0)
				continue;
			sb.append(String.format("%g,%d,%g,%g,%g,%g\n", b.getLower(), stats.getN(),
					stats.getMin(), stats.getMax(),
					stats.getMean(),
					stats.getStdDev()));
		}
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public double[] getLowerBounds() {
		return lowerBounds;
	}

	public void setLowerBounds(double[] lowerBounds) {
		this.lowerBounds = lowerBounds;
	}

	public Bucket getMruBucket() {
		return mruBucket;
	}

	public void setMruBucket(Bucket mruBucket) {
		this.mruBucket = mruBucket;
	}

	public void setBuckets(List<Bucket> buckets) {
		this.buckets = buckets;
	}
}
