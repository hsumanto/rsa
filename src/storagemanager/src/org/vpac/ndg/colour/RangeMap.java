package org.vpac.ndg.colour;

import org.vpac.ndg.common.NumberUtils;

public class RangeMap {

	private double sourceMin;
	private double sourceMax;
	private double compressedMin;
	private double compressedMax;

	public RangeMap(double sourceMin, double sourceMax,
			double compressedMin, double compressedMax) {
		this.sourceMin = sourceMin;
		this.sourceMax = sourceMax;
		this.compressedMin = compressedMin;
		this.compressedMax = compressedMax;
	}

	public double toFraction(double value) {
		return NumberUtils.unlerp(sourceMin, sourceMax, value);
	}

	public double compress(double value) {
		double fraction = NumberUtils.unlerp(this.sourceMin, this.sourceMax,
				value);
		return NumberUtils.lerp(this.compressedMin, this.compressedMax,
				fraction);
	}

}
