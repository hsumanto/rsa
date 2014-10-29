package org.vpac.ndg.colour;

import java.awt.Color;

import org.vpac.ndg.common.NumberUtils;

/**
 * A rainbow of colours, progressing smoothly through the colour spectrum.
 * @author Alex Fraser
 */
public class HueRotatePalette implements Palette {

	private RangeMap rangeMap;
	private double start;
	private double end;
	private float saturation;

	/**
	 * @param start The colour to use for the minimum value in the range,
	 *            between 0 and 1, with 0 being red, 1/3 being green, 2/3 being
	 *            blue and 1.0 being red again.
	 * @param end The colour to use for the maximum value in the range.
	 * @param saturation How saturated the colours should be, with 0 being all
	 *            grey and 1 being fully saturated.
	 */
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
        float hue = (float) NumberUtils.lerp(start, end, fraction);
        return Color.getHSBColor(hue, saturation, 1.0f);
	}

}
