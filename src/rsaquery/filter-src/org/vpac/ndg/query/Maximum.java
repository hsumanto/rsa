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

import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;

@Description(name = "Maximum", description = "Returns the greater value of its"
		+ " inputs. Can also be used as an 'or' filter for boolean data.")
@InheritDimensions(from = "in")
public class Maximum implements Filter {

	// Input fields.
	@Rank(promote = true, group = "in")
	public PixelSource inputA;
	@Rank(promote = true, group = "in")
	public PixelSource inputB;

	// Output fields.
	@CellType("inputA")
	public Cell output;

	Element<?> max;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {
		max = inputA.getPrototype().getElement().copy();
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		max.maxOf(inputA.getPixel(coords), inputB.getPixel(coords));
		output.set(max);
	}
}
