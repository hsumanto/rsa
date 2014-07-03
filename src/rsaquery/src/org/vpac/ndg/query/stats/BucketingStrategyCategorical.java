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

	static final double EPSILON = 1.0e-9;

	@Override
	public double[] computeBucketBounds(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value))
			return new double[] {value, value};

		double epsilon = value * EPSILON;
		return new double[] {value - epsilon, value + epsilon};
	}

}
