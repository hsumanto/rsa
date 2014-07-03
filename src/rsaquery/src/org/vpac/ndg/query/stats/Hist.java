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

	private static final long serialVersionUID = 2L;

	private String id;

	private List<Bucket> buckets;

	private BucketingStrategy bucketingStrategy;
	// The most-recently-used bucket.
	private Bucket mruBucket;

	public Hist() {
		bucketingStrategy = new BucketingStrategyCategorical();
		buckets = new ArrayList<Bucket>();
	}

	public Hist copy() {
		Hist res = new Hist();
		res.setBucketingStrategy(bucketingStrategy);
		for (Bucket b : buckets)
			res.buckets.add(b.copy());
		return res;
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
			else
				break;
		}

		Bucket b = null;
		if (i < 0 || !buckets.get(i).canContain(value)) {
			// No bucket can contain this value yet. Create one that can.
			// The buckets are always sorted, so we already know where to insert
			// the new bucket (discovered above).
			double[] bounds = bucketingStrategy.computeBucketBounds(value);
			b = new Bucket(bounds[0], bounds[1], new Stats());
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
		res.setBucketingStrategy(bucketingStrategy);
		res.setBuckets(targetBuckets);

		return res;
	}

	public Hist optimise() {
		Hist res = new Hist();
		res.setBucketingStrategy(bucketingStrategy);
		for (Bucket b : buckets) {
			if (b.getStats().getCount() > 0)
				res.buckets.add(b);
		}
		return res;
	}

	/**
	 * @return A single stats object that summarises all values in this
	 *         histogram.
	 */
	public Stats summarise() {
		Stats res = new Stats();
		for (Bucket b : buckets) {
			if (b.getStats().getCount() > 0)
				res = res.fold(b.getStats());
		}
		return res;
	}

	/**
	 * Create a new histogram containing only buckets that match some criteria.
	 *
	 * @param lower The lower bounds of the buckets. Parallel list with upper.
	 *            If null, all buckets will match.
	 * @param upper The upper bounds of the buckets. Parallel list with lower.
	 *            If null, all buckets will match.
	 * @return A new histogram containing only buckets that match one of the
	 *         lower-upper bound pairs.
	 */
	public Hist filterByRange(List<Double> lower, List<Double> upper) {
		if (lower == null && upper != null)
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		if (lower == null)
			return copy();

		if (lower.size() != upper.size())
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		Hist res = new Hist();
		res.setBucketingStrategy(bucketingStrategy);
		for (Bucket b : buckets) {
			for (int i = 0; i < lower.size(); i++) {
				if (b.getLower() >= lower.get(i) && b.getUpper() <= upper.get(i)) {
					res.buckets.add(b.copy());
					continue;
				}
			}
		}
		return res;
	}

	/**
	 * Create a new histogram containing only buckets that can contain certain
	 * values. This is especially useful for categorical data, where each bucket
	 * will only contain an exact value (not a range).
	 *
	 * @param values The values to search for. If null, all buckets will match.
	 * @return A new histogram containing only buckets that can contain one or
	 *         more of the values.
	 */
	public Hist filterByValue(List<Double> values) {
		if (values == null)
			return copy();

		Hist res = new Hist();
		res.setBucketingStrategy(bucketingStrategy);
		for (Bucket b : buckets) {
			for (Double value : values) {
				if (b.canContain(value)) {
					res.buckets.add(b.copy());
					continue;
				}
			}
		}
		return res;
	}

	public List<Bucket> getBuckets() {
		return buckets;
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

	public BucketingStrategy getBucketingStrategy() {
		return bucketingStrategy;
	}

	public void setBucketingStrategy(BucketingStrategy bucketingStrategy) {
		this.bucketingStrategy = bucketingStrategy;
	}
}
