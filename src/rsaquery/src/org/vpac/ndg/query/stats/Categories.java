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

import org.vpac.ndg.query.Description;
import org.vpac.ndg.query.Filter;
import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.filter.Accumulator;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.InheritDimensions;
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
 * Groups data into categories based on a metadata band.
 *
 * @author Alex Fraser
 */
@Description(name = "Categories", description = "Groups data into categories based on a metadata band. This is a pass-through filter with metadata collection.")
@InheritDimensions(from = "input")
public class Categories implements Filter, Accumulator<VectorCats> {

	public PixelSource input;
	public PixelSourceScalar categories;

	@CellType("input")
	public Cell output;

	private VectorCats stats;

	@Override
	public void initialise(BoxReal bounds) throws QueryConfigurationException {
		if ((!ElementInt.class.isAssignableFrom(categories.getPrototype().getElement().getClass())) &&
				(!ElementShort.class.isAssignableFrom(categories.getPrototype().getElement().getClass())) &&
				(!ElementByte.class.isAssignableFrom(categories.getPrototype().getElement().getClass())))
			throw new QueryConfigurationException("Categories must be byte, short or integer.");
		stats = new VectorCats(input.getPrototype().getElement());
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		Element<?> temp = input.getPixel(coords);
		ScalarElement cat = categories.getScalarPixel(coords);
		stats.update(cat, temp);
		output.set(temp);
	}

	@Override
	public VectorCats getAccumulatedOutput() {
		return stats;
	}

}
