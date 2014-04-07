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

package org.vpac.ndg.query;

import java.io.IOException;

import org.vpac.ndg.query.filter.Accumulator;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.CellType;
import org.vpac.ndg.query.sampling.PixelSource;

/**
 * Accumulates statistics metadata (min, max, etc.) for the whole image.
 *
 * @author Alex Fraser
 */
@Description(name = "Statistics", description = "Find mean, max, etc. for all pixels.")
@InheritDimensions(from = "input")
public class Statistics implements Filter, Accumulator<Statistics.Stats> {

	public PixelSource input;

	@CellType("input")
	public Cell output;

	private Stats stats;

	@Override
	public void initialise(BoxReal bounds) throws QueryConfigurationException {
		stats = new Stats(input.getPrototype().getElement());
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		Element<?> temp = input.getPixel(coords);
		stats.update(temp);
		output.set(temp);
	}

	@Override
	public Stats getAccumulatedOutput() {
		return stats;
	}

	public class Stats implements Foldable<Statistics.Stats> {

		private Element<?> min;
		private Element<?> max;

		public Stats(Element<?> prototype) {
			min = prototype.copy().maximise();
			max = prototype.copy().minimise();
		}

		void update(Element<?> value) {
			min.min(value);
			max.max(value);
		}

		@Override
		public Foldable<Stats> fold(Stats other) {
			Stats res = new Stats(min);
			res.min = min.minNew(other.min);
			res.max = max.minNew(other.max);
			return res;
		}

		@Override
		public String toString() {
			return String.format("min: %s, max: %s", min, max);
		}

	}

}
