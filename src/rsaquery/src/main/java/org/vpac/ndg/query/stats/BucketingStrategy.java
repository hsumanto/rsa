package org.vpac.ndg.query.stats;

import org.vpac.ndg.query.QueryException;

public interface BucketingStrategy {

	/**
	 * Calculate the upper and lower bounds for a bucket that could contain
	 * some value.
	 * @param value The value that the bucket should contain.
	 * @return A two-element array of {lower bound, upper bound}.
	 */
	double[] computeBucketBounds(double value);

	boolean isCategorical();

	void checkConfiguration() throws QueryException;

	String getDef();

}
