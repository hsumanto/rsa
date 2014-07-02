package org.vpac.ndg.query.stats;

import java.io.Serializable;


public class BucketingStrategyDecimal implements BucketingStrategy, Serializable {

	private static final long serialVersionUID = 1L;

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final double EPSILON = 1.0e-9;

	private double base;
	private double root;
	private double scale;

	public BucketingStrategyDecimal() {
		base = BASE;
		root = BUCKETS_PER_ORDER_OF_MAGNITUDE;
		scale = SCALE;
	}

	@Override
	public double[] computeBucketBounds(double value) {
		boolean negative = value < 0;

		
		if (Double.isInfinite(value)) {
			if (negative) {
				return new double[] {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
			} else {
				return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
			}
		} else if (Double.isNaN(value)) {
			return new double[] {Double.NaN, Double.NaN};
		}

		value = Math.abs(value);

		double lower;
		double upper;

		if (value < scale) {
			lower = 0.0;
			upper = lowerBound(0, base, root, scale);
		} else {
			int i = indexOf(value, base, root, scale);
			if (i < 0) {
				lower = 0.0;
				upper = lowerBound(0, base, root, scale);
			} else {
				lower = lowerBound(i, base, root, scale);
				upper = lowerBound(i + 1, base, root, scale);
			}
		}

		// No need to adjust values with an epsilon: the upper bound is computed
		// using an integer (+1), so it will always be exactly what would have
		// been calculated for the next bucket.

		if (negative) {
			double temp = lower;
			lower = -upper;
			upper = -temp;
		}

		return new double[] {lower, upper};
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
		double index = (logarithm * root) + ((root * Math.pow(base, logarithm % 1.0)) / base);
		double value = Math.pow(base, logarithm) / scale;
	}

	/**
	 * Find the bucket "index" (offset from zero) for some value.
	 * @param value The value to find a bucket for.
	 * @param root The number of buckets per order of magnitude.
	 * @param base The base order.
	 * @param scale The scaling factor (lower bound of bucket 0).
	 * @return The "index" of the bucket.
	 */
	static int indexOf(double value, double base, double root, double scale) {
		double logarithm = logN(base, value * scale) + EPSILON;
		double major = Math.floor(logarithm) * root;
		double minor = Math.floor((root * Math.pow(base, logarithm % 1.0)) / base);
		double index = major + minor;
		index += EPSILON;
		return (int) index;
	}

	static double logN(double base, double value) {
		// The log to any base can be found from the natural logarithm
		// http://www.themathpage.com/aPreCalc/logarithms.htm#change
		// http://blog.dreasgrech.com/2010/02/finding-logarithm-of-any-base-in-java.html
		return Math.log(value) / Math.log(base);
	}
}
