/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2014 VPAC Innovations Pty Ltd
 * http://vpac-innovations.com.au
 */

package org.vpac.ndg.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.math.ElementByte;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.CellScalar;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

/**
 * Generates a mask of pixels that are within a set of bounds.
 * @author Alex Fraser
 */
@InheritDimensions(from = "input")
public abstract class In implements Filter {

	public PixelSourceScalar input;
	public double EPSILON = 0.00001;

	@CellType("byte")
	public CellScalar output;

	private List<Double> lowerBounds;
	private List<Double> upperBounds;
	private ScalarElement match = new ElementByte((byte) 1);
	private ScalarElement fail = new ElementByte((byte) 0);

	protected void setBounds(String[] lbs, String[] ubs)
			throws QueryException {

		if (lbs.length != ubs.length) {
			throw new QueryBindingException("Literal inputs 'lower' and"
					+ " 'upper' are parallel arrays and must have the same"
					+ " number of elements.");
		}

		try {
			lowerBounds = new ArrayList<Double>(lbs.length);
			for (String lb : lbs) {
				// Shift value a little bit to account for loss of numerical
				// precision.
				double v = Double.parseDouble(lb);
				double epsilon;
				if (v > 0)
					epsilon = EPSILON;
				else
					epsilon = -EPSILON;
				v -= v * epsilon;
				lowerBounds.add(v);
			}
			upperBounds = new ArrayList<Double>(ubs.length);
			for (String ub : ubs) {
				double v = Double.parseDouble(ub);
				double epsilon;
				if (v > 0)
					epsilon = EPSILON;
				else
					epsilon = -EPSILON;
				v += v * epsilon;
				upperBounds.add(v);
			}
		} catch (NumberFormatException e) {
			throw new QueryBindingException("Failed to parse bounds.", e);
		}
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		ScalarElement elem = input.getScalarPixel(coords);
		if (!elem.isValid()) {
			output.unset();
			return;
		}
		for (int i = 0; i < lowerBounds.size(); i++) {
			double lb = lowerBounds.get(i);
			double ub = upperBounds.get(i);
			if (elem.compareTo(lb) >= 0 && elem.compareTo(ub) <= 0) {
				output.set(match);
				return;
			}
		}
		output.set(fail);
	}
}
