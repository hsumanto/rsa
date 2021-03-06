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
 * Copyright 2014 VPAC Innovations
 */

package org.vpac.ndg.query.stats;

import java.io.IOException;

import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.Accumulator;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;

/**
 * Accumulates statistics metadata (min, max, etc.) for the whole image.
 *
 * @author Alex Fraser
 */
@Description(name = "Statistics", description = "Finds mean, max, etc. for all pixels. This is a pass-through filter with metadata collection.")
@InheritDimensions(from = "input")
public class Statistics implements Filter, Accumulator<VectorStats> {

	public PixelSource input;

	@CellType("input")
	public Cell output;

	private VectorStats stats;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {
		stats = new VectorStats(input.getPrototype().getElement().size());
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		Element<?> temp = input.getPixel(coords);
		stats.update(temp);
		output.set(temp);
	}

	@Override
	public VectorStats getAccumulatedOutput() {
		return stats;
	}

}
