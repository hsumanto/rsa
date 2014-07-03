package org.vpac.ndg.query.stats;

/**
 * Creates histogram buckets that each contain only one value. For use with
 * categorical data.
 *
 * @author Alex Fraser
 */
public class BucketingStrategyCategorical implements BucketingStrategy {

	static final double EPSILON = 1.0e-9;

	@Override
	public double[] computeBucketBounds(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value))
			return new double[] {value, value};

		double epsilon = value * EPSILON;
		return new double[] {value - epsilon, value + epsilon};
	}

}
