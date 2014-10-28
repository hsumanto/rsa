package org.vpac.ndg.colour;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedPalette {

	private static final Logger log = LoggerFactory.getLogger(NamedPalette.class);

	public static Palette get(String name, double min, double max) {
		RangeMap rangeMap;
		switch (name) {
		case "rainbow240":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HueRotatePalette(rangeMap, 0, 2.0 / 3.0, 1);

		case "cyclic11":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new CyclicPalette(rangeMap, CYCLIC_11);

		case "hash":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HashPalette(rangeMap);

		default:
			log.warn("Unrecognised palette '{}'. Using 'rainbow360'", name);
		case "rainbow360":
			rangeMap = new RangeMap(min, max, 1, 255);
			return new HueRotatePalette(rangeMap, 0, 1, 1);
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
