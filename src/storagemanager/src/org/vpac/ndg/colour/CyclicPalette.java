package org.vpac.ndg.colour;

import java.awt.Color;

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
