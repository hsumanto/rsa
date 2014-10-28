package org.vpac.ndg.colour;

import java.awt.Color;

/**
 * Picks colours from a fixed-sized array. If sequence of colours repeats for
 * values that would fall outside the array.
 * @author Alex Fraser
 */
public class CyclicPalette implements Palette {

	private Color[] colours;
	private RangeMap rangeMap;

	public CyclicPalette(RangeMap rangeMap, Color[] colours) {
		this.rangeMap = rangeMap;
		this.colours = colours;
	}

	@Override
	public Color get(double value) {
		double cvalue = this.rangeMap.compress(value);
        int index = (int) (cvalue % this.colours.length);
        return this.colours[index];
	}

}
