package org.vpac.ndg.colour;

import java.awt.Color;

/**
 * Converts numerical values into colours.
 * @author Alex Fraser
 */
public interface Palette {

	Color get(double value);

}
