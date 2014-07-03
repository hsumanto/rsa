package org.vpac.ndg.query.stats;

import java.io.Serializable;

/**
 * Creates histogram buckets that each contain only one value. For use with
 * categorical data.
 *
 * @author Alex Fraser
 */
public class BucketingStrategyCategorical implements BucketingStrategy,
		Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public double[] computeBucketBounds(double value) {
		return new double[] {value, value};
	}

	@Override
	public boolean isCategorical() {
		return true;
	}
}
