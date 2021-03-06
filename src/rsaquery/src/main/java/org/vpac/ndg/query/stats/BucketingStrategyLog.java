package org.vpac.ndg.query.stats;

import java.io.Serializable;

import org.vpac.ndg.query.QueryException;


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

		double[] bucket = new double[2];

		int i = indexOf(value);
		createBucket(i, bucket);

		// Because we are using float arithmetic, sometimes the computed bucket
		// bounds will not quite include the requested value. In that case,
		// offset the index and request again.
		if (!negative) {
			if (value < bucket[0])
				createBucket(i - 1, bucket);
			else if (value >= bucket[1])
				createBucket(i + 1, bucket);
		} else {
			// For negative numbers, the inequality tests are different because
			// the lower and upper bounds will be swapped below.
			if (value <= bucket[0])
				createBucket(i - 1, bucket);
			else if (value > bucket[1])
				createBucket(i + 1, bucket);
		}

		// No need to adjust values with an epsilon: the upper bound is computed
		// using an integer (+1), so it will always be exactly what would have
		// been calculated for the next bucket.

		if (negative) {
			double temp = bucket[0];
			bucket[0] = -bucket[1];
			bucket[1] = -temp;
			value = -value;
		}

		if (bucket[0] == -0.0)
			bucket[0] = 0.0;
		if (bucket[1] == -0.0)
			bucket[1] = 0.0;

		return bucket;
	}

	private void createBucket(int i, double[] bucket) {
		if (i < 0) {
			bucket[0] = 0.0;
			bucket[1] = lowerBound(0);
		} else {
			bucket[0] = lowerBound(i);
			bucket[1] = lowerBound(i + 1);
		}
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
		if (value < scale)
			return -1;
		double index = logN(base, value / scale) * n;
		return (int) index;
	}

	double logN(double base, double value) {
		// The log to any base can be found from the natural logarithm
		// http://www.themathpage.com/aPreCalc/logarithms.htm#change
		// http://blog.dreasgrech.com/2010/02/finding-logarithm-of-any-base-in-java.html
		return Math.log(value) / Math.log(base);
	}

	@Override
	public void checkConfiguration() throws QueryException {
		if (base <= 1.0) {
			throw new QueryException(
					"Log bucketing strategy: base must be greater than 1.");
		}
		if (n < 1.0) {
			throw new QueryException(
					"Log bucketing strategy: n must be at least 1.");
		}
		if (scale < Double.MIN_NORMAL) {
			throw new QueryException(
					"Log bucketing strategy: scale is too small.");
		}
	}

	@Override
	public String getDef() {
		return String.format("log/base/%s/n/%s/scale/%s", base, n, scale);
	}

	@Override
	public String toString() {
		return String.format("BucketingStrategyLog(%s)", getDef());
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BucketingStrategyLog))
			return false;
		BucketingStrategyLog b = (BucketingStrategyLog) other;
		if (b.base != base)
			return false;
		if (b.n != n)
			return false;
		if (b.scale != scale)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;
		long d;
		d = Double.doubleToLongBits(base);
		result = 37 * result + (int)(d ^ (d >>> 32));
		d = Double.doubleToLongBits(n);
		result = 37 * result + (int)(d ^ (d >>> 32));
		d = Double.doubleToLongBits(scale);
		result = 37 * result + (int)(d ^ (d >>> 32));
		return result;
	}
}
