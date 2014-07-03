package org.vpac.ndg.query.stats;


/**
 * Creates histogram buckets that have both logarithmic and quantile
 * characteristics.
 *
 * <p>
 * Each order of magnitude is divided into <em>n</em> equal-sized buckets.
 * </p>
 *
 * @author Alex Fraser
 */
public class BucketingStrategyLogQuantile extends BucketingStrategyLog {

	private static final long serialVersionUID = 1L;

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
