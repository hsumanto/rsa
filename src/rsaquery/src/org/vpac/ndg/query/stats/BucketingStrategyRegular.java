package org.vpac.ndg.query.stats;

/**
 * Generates buckets at regular intervals.
 *
 * <p>
 * For example, with an origin of <em>5</em> and a width of <em>10</em>, the
 * sequence would be <em>..., -15, -5, 5, 15, 25, ... </em>.
 * </p>
 * @author Alex Fraser
 */
public class BucketingStrategyRegular implements BucketingStrategy {

	public double origin = 0.0;
	public double width = 20.0;

	public void setOrigin(String origin) {
		this.origin = Double.parseDouble(origin);
	}

	public void setWidth(String width) {
		this.width = Double.parseDouble(width);
	}

	@Override
	public double[] computeBucketBounds(double value) {
		double index = Math.floor((value - origin) / width);
		double lower = origin + ((index) * width);
		double upper = origin + ((index + 1) * width);
		return new double[] {lower, upper};
	}

	public boolean isCategorical() {
		return false;
	};
}
