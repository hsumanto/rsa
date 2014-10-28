package org.vpac.ndg.colour;

import java.awt.Color;

import org.vpac.ndg.common.NumberUtils;

public class HashPalette implements Palette {

	private RangeMap rangeMap;

	public HashPalette(RangeMap rangeMap) {
		this.rangeMap = rangeMap;
	}

	@Override
	public Color get(double value) {
        double cvalue = this.rangeMap.compress(value);
        double hashedValue = hash((int) cvalue);
        String hex = NumberUtils.toHexFraction(hashedValue, 6);
        return Color.decode("0x" + hex);
	}

    double hash(int x) {
        // Robert Jenkins' 32-bit integer hash function.
        // http://stackoverflow.com/a/3428186/320036
        // The prime given on the first line happens to give a good result
        // for boolean datasets.
        int seed = x ^ 1376312589;
        seed = ((seed + 0x7ed55d16) + (seed << 12))  & 0xffffffff;
        seed = ((seed ^ 0xc761c23c) ^ (seed >>> 19)) & 0xffffffff;
        seed = ((seed + 0x165667b1) + (seed << 5))   & 0xffffffff;
        seed = ((seed + 0xd3a2646c) ^ (seed << 9))   & 0xffffffff;
        seed = ((seed + 0xfd7046c5) + (seed << 3))   & 0xffffffff;
        seed = ((seed ^ 0xb55a4f09) ^ (seed >>> 16)) & 0xffffffff;
        return (seed & 0xfffffff) / 268435456.0;
    };

}
