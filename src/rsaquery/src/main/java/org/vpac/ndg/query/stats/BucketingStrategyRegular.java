package org.vpac.ndg.query.stats;

import java.io.Serializable;

import org.vpac.ndg.query.QueryException;

/**
 * Generates buckets at regular intervals.
 *
 * <p>
 * For example, with an origin of <em>5</em> and a width of <em>10</em>, the
 * sequence would be <em>..., -15, -5, 5, 15, 25, ... </em>. By default, the
 * origin is 0 and the width is 20.
 * </p>
 * @author Alex Fraser
 */
public class BucketingStrategyRegular implements BucketingStrategy, Serializable{

	private static final long serialVersionUID = 1L;

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

	@Override
	public void checkConfiguration() throws QueryException {
		if (width < Double.MIN_NORMAL) {
			throw new QueryException(
				"Regular bucketing strategy: width is too small.");
		}
	}

	@Override
	public String getDef() {
		return String.format("regular/origin/%s/width/%s", origin, width);
	}

	@Override
	public String toString() {
		return String.format("BucketingStrategyRegular(%s)", getDef());
	}
}
