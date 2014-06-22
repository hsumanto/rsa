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

package org.vpac.web.query;

import java.util.List;

import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.ElementByte;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.sampling.CellScalar;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

@Description(name = "In Set", description = "Generates a mask of pixels that are contained in a set.")
@InheritDimensions(from = "input")
public class InSet extends InRange {

	// Input fields.
	public PixelSourceScalar input;

	// Comma-separated list of IDs.
	public String ids;

	// Output fields.
	@CellType("byte")
	public CellScalar output;

	List<Double> lowerBounds;
	List<Double> upperBounds;
	public double EPSILON = 0.00001;
	ScalarElement match = new ElementByte((byte) 1);
	ScalarElement fail = new ElementByte((byte) 0);

	@Override
	public void initialise(BoxReal bounds) throws QueryConfigurationException {
		String[] keys = ids.split(",");
		setBounds(keys, keys);
	}

}