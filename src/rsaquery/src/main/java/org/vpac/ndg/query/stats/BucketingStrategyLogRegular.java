package org.vpac.ndg.query.stats;

import org.vpac.ndg.query.QueryException;


/**
 * Creates histogram buckets that have both logarithmic and regular
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
public class BucketingStrategyLogRegular extends BucketingStrategyLog {

	private static final long serialVersionUID = 1L;

	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 5.0;

	public BucketingStrategyLogRegular() {
		super();
		setN(BUCKETS_PER_ORDER_OF_MAGNITUDE);
	}

	@Override
	double lowerBound(int i) {
		// Lower bound = n^(floor(i/nb)+logn(i % nb / nb) + 1)

		double wholePart = Math.floor(i / n);

		double fraction = logN(base, (i % n) / n) + 1;
		if (fraction < 0)
			fraction = 0;

		double lower = Math.pow(base, wholePart + fraction);
		lower *= scale;
		return lower;
	}

	@Override
	int indexOf(double value) {
		if (value < scale)
			return -1;
		double logarithm = logN(base, value / scale);
		double wholePart = Math.floor(logarithm) * n;
		double fraction = Math.floor((n * Math.pow(base, logarithm % 1.0)) / base);
		double index = wholePart + fraction;
		return (int) index;
	}

	@Override
	public void checkConfiguration() throws QueryException {
		super.checkConfiguration();
		if (base - n < 0.1) {
			throw new QueryException(
				"LogRegular bucketing strategy: base must be greater than n.");
		}
	}

	@Override
	public String getDef() {
		return String.format(
			"logRegular/base/%s/n/%s/scale/%s",
			base, n, scale);
	}

	@Override
	public String toString() {
		return String.format("BucketingStrategyLogRegular(%s)", getDef());
	}
}
