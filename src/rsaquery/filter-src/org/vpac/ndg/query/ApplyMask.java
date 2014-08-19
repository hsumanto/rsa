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

import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

@Description(name = "Apply Mask", description = "Returns input (pass-through) for non-zero mask pixels, or nodata for zero mask pixels.")
@InheritDimensions(from = "in")
public class ApplyMask implements Filter {

	// Input fields.
	@Rank(promote = true, group = "in")
	public PixelSource input;
	@Rank(promote = true, group = "in")
	public PixelSourceScalar mask;

	// Output fields.
	@CellType("input")
	public Cell output;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		ScalarElement val = mask.getScalarPixel(coords);
		if (val.isValid() && val.byteValue() != 0)
			output.set(input.getPixel(coords));
		else
			output.unset();
	}
}
