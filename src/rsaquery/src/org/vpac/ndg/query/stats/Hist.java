package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Groups values together based on their intrinsic distribution.
 *
 * @author Alex Fraser
 */
public class Hist implements Foldable<Hist>, Serializable {

	private static final long serialVersionUID = 1L;

	// Beware! If you change these, old histograms will not be comparable to
	// new ones.
	private String id;
	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	private List<Bucket> buckets;

	private Bucket mruBucket;

	public Hist() {
		double[] boundaries = genBoundaries(BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE,
				SCALE, NUM_BUCKETS);

		buckets = new ArrayList<Bucket>();
		for (int i = 0; i < boundaries.length - 1; i++) {
			buckets.add(new Bucket(boundaries[i], boundaries[i + 1],
					new Stats()));
		}
	}

	public void update(ScalarElement value) {
		if (!value.isValid())
			return;

		double v = value.doubleValue();
		getBucket(v).getStats().update(value);
	}

	private Bucket getBucket(double value) {
		if (mruBucket != null && mruBucket.canContain(value))
			return mruBucket;

		int i = -1;
		for (int j = 0; j < buckets.size(); j++) {
			Bucket b = buckets.get(j);
			if (b.getLower() <= value)
				i = j;
		}

		Bucket b = null;
		if (i < 0 || !buckets.get(i).canContain(value)) {
			// No bucket can contain this value! This must be a categorical
			// dataset, so create a new bucket to store this value.
			b = new Bucket(value, value, new Stats());
			buckets.add(i + 1, b);
		} else {
			b = buckets.get(i);
		}
		mruBucket = b;

		return mruBucket;
	}

	/**
	 * Combine this histogram with another to create a new object that contains
	 * the information from both.
	 *
	 * <p>
	 * <b>Warning</b>: Any buckets in <em>other</em> that overlap buckets in
	 * this histogram will be merged together in the new object. You should
	 * only fold histograms that have been created with the same bucketing
	 * scheme.
	 * </p>
	 */
	@Override
	public Hist fold(Hist other) {

		List<Bucket> sourceBuckets = new ArrayList<Bucket>();
		sourceBuckets.addAll(this.buckets);
		sourceBuckets.addAll(other.buckets);
		Collections.sort(sourceBuckets, new Comparator<Bucket>() {
			@Override
			public int compare(Bucket o1, Bucket o2) {
				return Double.compare(o1.getLower(), o2.getLower());
			}
		});

		Bucket currentBucket = null;
		List<Bucket> targetBuckets = new ArrayList<Bucket>();
		for (Bucket b : sourceBuckets) {
			if (currentBucket == null) {
				currentBucket = b.copy();
			} else if (currentBucket.intersects(b)) {
				currentBucket = currentBucket.fold(b);
			} else {
				targetBuckets.add(currentBucket);
				currentBucket = b.copy();
			}
		}

		if (currentBucket != null)
			targetBuckets.add(currentBucket);

		Hist res = new Hist();
		res.setBuckets(targetBuckets);

		return res;
	}

	public Hist optimise() {
		Hist res = new Hist();
		res.buckets.clear();
		for (Bucket b : buckets) {
			if (b.getStats().getCount() > 0)
				res.buckets.add(b);
		}
		return res;
	}

	public List<Bucket> getBuckets() {
		return buckets;
	}

	/**
	 * @return A single stats object that summarises all values in this
	 *         histogram.
	 */
	public Stats getSummary() {
		Stats res = new Stats();
		for (Bucket b : buckets) {
			if (b.getStats().getCount() > 0)
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
	 * @param base The base order.
	 * @param root The number of buckets per order of magnitude.
	 * @param scale The scaling factor.
	 * @param n The number of buckets to generate.
	 * @return An array of bucket boundaries.
	 */
	static double[] genBoundaries(double base, double root, double scale, int n) {

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
	 * @param i The index to generate a lower bound of.
	 * @param base The base order.
	 * @param root The number of buckets per order of magnitude.
	 * @param scale The scaling factor.
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

			if (b.getStats().getCount() <= 0)
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
			sb.append(String.format("%g,%d,%g,%g,%g,%g\n", b.getLower(),
					stats.getCount(),
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
