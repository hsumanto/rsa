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
 * Copyright 2016 VPAC Innovations
 */

package org.vpac.ndg.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vpac.ndg.query.QueryBindingException;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.Accumulator;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ElementByte;
import org.vpac.ndg.query.math.ElementInt;
import org.vpac.ndg.query.math.ElementShort;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

/**
 * Outputs a single band of nodata.
 *
 * @author Alex Fraser
 */
@Description(name = "Null", description = "Reads all inputs, but outputs only a single band filled with nodata")
@InheritDimensions(from = "input")
public class Null implements Filter {

	public PixelSource input;

	@CellType("byte")
	public Cell output;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		input.getPixel(coords);
		output.unset();
	}

}
