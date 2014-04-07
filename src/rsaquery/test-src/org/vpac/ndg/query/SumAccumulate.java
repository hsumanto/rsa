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
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.CellType;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

/**
 * Accumulates a value for the whole image.
 *
 * @author Alex Fraser
 */
@Description(name = "Accumulate Sum", description = "Sum the values of all pixels in an image.")
@InheritDimensions(from = "input")
public class SumAccumulate implements Filter,
		Accumulator<SumAccumulate.IntAdder> {

	public PixelSourceScalar input;

	@CellType("input")
	public Cell output;

	private IntAdder adder;

	@Override
	public void initialise(BoxReal bounds) throws QueryConfigurationException {
		adder = new IntAdder(0);
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		ScalarElement temp = input.getScalarPixel(coords);
		adder.add(temp.longValue());
		output.set(temp);
	}

	@Override
	public IntAdder getAccumulatedOutput() {
		return adder;
	}

	public class IntAdder implements Foldable<IntAdder> {

		private long value;

		public IntAdder(long value) {
			this.value = value;
		}

		void add(long other) {
			this.value += other;
		}

		@Override
		public Foldable<IntAdder> fold(IntAdder other) {
			return new IntAdder(value + other.value);
		}

		@Override
		public String toString() {
			return Long.toString(value);
		}

	}
}
