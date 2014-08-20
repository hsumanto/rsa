package org.vpac.ndg.query.stats;


/**
 * Creates histogram buckets that have both logarithmic and quantile
 * characteristics.
 *
 * <p>
 * Each order of magnitude is divided into <em>n</em> equal-sized buckets -
 * except for the first bucket in each, which is smaller than the others by
 * the size of the previous order of magnitude.
 * </p>
 *
 * <p>
 * For example, the following is a list of bucket bounds and the size of each
 * bucket (second line). Notice that the size of bucket 1.0-3.3 is 2.3, while
 * the others in that order of magnitude are 3.3.
 * </p>
 *
 * <pre>
 * 0.00, 0.33, 0.66, 1.0, 3.3, 6.6, 10, 33, 66, 100
 *   0.33, 0.33, 0.33, 2.3, 3.3, 3.3, 23, 33, 33
 * </pre>
 *
 * @author Alex Fraser
 */
public class BucketingStrategyLogQuantile extends BucketingStrategyLog {

	private static final long serialVersionUID = 1L;

	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 5.0;

	public BucketingStrategyLogQuantile() {
		super();
		setN(BUCKETS_PER_ORDER_OF_MAGNITUDE);
	}

	@Override
	double lowerBound(int i) {
		// Lower bound = n^(floor(i/nb)+logn(i % nb / nb) + 1)

		double wholePart = Math.floor(i / n);

		double fraction;
		if (i % n < EPSILON)
			fraction = 0;
		else
			fraction = logN(base, (i % n) / n) + 1;

		double lower = Math.pow(base, wholePart + fraction);
		lower *= scale;
		return lower;
	}

	@Override
	int indexOf(double value) {
		double logarithm = logN(base, value / scale) + EPSILON;
		double wholePart = Math.floor(logarithm) * n;
		double fraction = Math.floor((n * Math.pow(base, logarithm % 1.0)) / base);
		double index = wholePart + fraction;
		index += EPSILON;
		return (int) index;
	}
}
