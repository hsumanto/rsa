package org.vpac.ndg.colour;

import java.awt.Color;

import org.vpac.ndg.common.NumberUtils;

public class HueRotatePalette implements Palette {

	private RangeMap rangeMap;
	private double start;
	private double end;
	private float saturation;

	public HueRotatePalette(RangeMap rangeMap, double start, double end,
			float saturation) {
		this.rangeMap = rangeMap;
		this.start = start;
		this.end = end;
		this.saturation = saturation;
	}

	@Override
	public Color get(double value) {
        double fraction = this.rangeMap.toFraction(value);
        float hue = (float) NumberUtils.unlerp(start, end, fraction);
        return Color.getHSBColor(hue, saturation, 1.0f);
	}

}
