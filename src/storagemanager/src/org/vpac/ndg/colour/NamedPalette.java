package org.vpac.ndg.colour;

import java.awt.Color;

public class NamedPalette {

	/**
	 * Get a named palette.
	 *
	 * <p>
	 * The minimum and maximum parameters refer to the range of the input set;
	 * the values will generally be scaled to range supported by the palette
	 * that is being chosen. Many palettes have 255 colours; in that case,
	 * min will be mapped to 1 and max mapped to 255. See {@link RangeMap} for
	 * details.
	 * </p>
	 *
	 * @param name The name of the palette to get.
	 * @param min The minimum value of the input.
	 * @param max The maximum value of the input.
	 * @return The palette.
	 */
	public static Palette get(String name, double min, double max) {
		RangeMap rangeMap;
		switch (name) {
		case "rainbow240":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HueRotatePalette(rangeMap, 0, 2.0 / 3.0, 1);

		case "cyclic11":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new CyclicPalette(rangeMap, CYCLIC_11);

		case "hash255":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HashPalette(rangeMap);

		case "rainbow360":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HueRotatePalette(rangeMap, 0, 1, 1);

		default:
			throw new IllegalArgumentException(String.format(
					"Unrecognised palette '%s'. Using 'rainbow360'", name));
		}
	}

	private static final Color[] CYCLIC_11 = {
		new Color(31, 120, 180),
		new Color(227, 26, 28),
		new Color(178, 223, 138),
		new Color(51, 160, 44),
		new Color(251, 154, 153),
		new Color(166, 206, 227),
		new Color(253, 191, 111),
		new Color(255, 127, 0),
		new Color(202, 178, 214),
		new Color(106, 61, 154),
		new Color(255, 255, 153)
	};

}
