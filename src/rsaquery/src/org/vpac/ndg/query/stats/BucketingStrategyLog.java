package org.vpac.ndg.query.stats;

import java.io.Serializable;


/**
 * Creates histogram buckets that increase in size the further they are from
 * zero.
 *
 * @author Alex Fraser
 */
public class BucketingStrategyLog implements BucketingStrategy, Serializable {

	private static final long serialVersionUID = 1L;

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final double EPSILON = 1.0e-9;

	protected double base;
	protected double n;
	protected double scale;

	public BucketingStrategyLog() {
		base = BASE;
		n = BUCKETS_PER_ORDER_OF_MAGNITUDE;
		scale = SCALE;
	}

	public double getScale() {
		return scale;
	}

	/**
	 * @param scale The size of the smallest bucket, e.g. if set to 0.1 then the
	 *            first positive bucket will range from zero to 0.1. This is
	 *            used as a scaling factor for all buckets.
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getN() {
		return n;
	}

	/**
	 * @param n The number of buckets in each order of magnitude.
	 */
	public void setN(double n) {
		this.n = n;
	}

	public double getBase() {
		return base;
	}

	/**
	 * @param base The number of integers that comprise an order of magnitude.
	 * @see <a href="http://en.wikipedia.org/wiki/Base_(exponentiation)">
	 * 	          Wikipedia's article on bases</a>
	 */
	public void setBase(double base) {
		this.base = base;
	}

	@Override
	public boolean isCategorical() {
		return false;
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
			upper = lowerBound(0);
		} else {
			int i = indexOf(value);
			if (i < 0) {
				lower = 0.0;
				upper = lowerBound(0);
			} else {
				lower = lowerBound(i);
				upper = lowerBound(i + 1);
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

		// Adjust for rounding errors. This may cause some buckets to overlap
		// by a small fraction.
		// TODO: try to do this without buckets overlapping.
		if (lower > value)
			lower = value;
		if (upper < value)
			upper = value;

		return new double[] {lower, upper};
	}

	/**
	 * Computes the lower bound of a bucket. This only generates lower bounds
	 * for positive indices.
	 *
	 * @param i The index to find the lower bound of.
	 * @return The lower bound of the bucket.
	 */
	double lowerBound(int i) {
		return Math.pow(base, i / n) * scale;
	}

	/**
	 * Find the bucket "index" (offset from zero) for some value. This is an
	 * encoding of the bucket lower bound, comprising the number of whole orders
	 * of magnitude (index / n) and the bucket offset within that order
	 * (index % n).
	 *
	 * @param value The value to find a bucket for.
	 * @return The "index" of the bucket.
	 */
	int indexOf(double value) {
		double index = logN(base, value / scale) * n;
		index += EPSILON;
		return (int) index;
	}

	double logN(double base, double value) {
		// The log to any base can be found from the natural logarithm
		// http://www.themathpage.com/aPreCalc/logarithms.htm#change
		// http://blog.dreasgrech.com/2010/02/finding-logarithm-of-any-base-in-java.html
		return Math.log(value) / Math.log(base);
	}
}
