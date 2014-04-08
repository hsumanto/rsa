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
import org.vpac.ndg.query.math.ScalarElement;
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

	/**
	 * Basic statistics (min, max, mean, standard deviation).
	 * @author Alex Fraser
	 */
	static public class Stats implements Foldable<Statistics.Stats> {

		public Element<?> min;
		public Element<?> max;
		public Element<?> mean;

		// M2 = variance * (n - 1)
		// Variance is the square of the standard deviation.
		private Element<?> M2;
		private long n;

		// Supporting fields. These aren't part of the useful output, but we
		// declare them here to prevent calling new for them for each value.
		private Element<?> delta1;
		private Element<?> delta2;
		private Element<?> deltaProportional;

		public Stats(Element<?> prototype) {
			min = prototype.copy().maximise();
			max = prototype.copy().minimise();

			n = 0;
			M2 = prototype.asDouble().set(0.0);
			mean = prototype.asDouble().set(0.0);

			delta1 = prototype.asDouble();
			delta2 = prototype.asDouble();
			deltaProportional = prototype.asDouble();
		}

		void update(Element<?> value) {
			min.min(value);
			max.max(value);

			// We only get one pass, so we have to use an online algorithm for
			// finding the variance. This is converted to the standard
			// deviation in getStdDev(). This algorithm is due to Knuth.
			n += 1;
			delta1.subOf(value, mean);
			deltaProportional.divOf(delta1, n);
			mean.add(deltaProportional);
			delta2.subOf(value, mean);
			M2.add(delta1.mul(delta2));
		}

		@Override
		public Stats fold(Stats other) {
			Stats res = new Stats(min);
			res.min = min.minNew(other.min);
			res.max = max.minNew(other.max);

			// Algorithm for combining parallel-processed variance is due to
			// Chan et. al. with modification for stability suggested on
			// Wikipedia.
			// http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm

			double deltaWeight;
			Element<?> delta;
			Element<?> deltaSq;

			// nX = nA + nB
			res.n = n + other.n;

			// delta = meanB - meanA
			delta = other.mean.subNew(mean);

			if (other.n < 10 || other.n < n / 10) {
				// For small n, or where nB is much smaller than nA.
				// meanX = meanA + delta * nB/nX
				deltaWeight = (double)other.n / (double)res.n;
				res.mean.addOf(mean, delta.mulNew(deltaWeight));
			} else {
				// For large n.
				// meanX = ((nA * meanA) + (nB * meanB)) / (nA + nB)
				res.mean = mean.mulNew(n).add(other.mean.mulNew(other.n)).
						divNew(res.n);
			}
			// (meanB - meanA)^2
			deltaSq = delta.mulNew(delta);

			deltaWeight = ((double)n * (double)other.n) / (double)res.n;
			res.M2 = M2.addNew(other.M2).add(deltaSq.mulNew(deltaWeight));

			// The delta fields are re-calculated for each input, so we don't
			// need to fold them in.

			return res;
		}

		public Element<?> getStdDev() {
			Element<?> variance = M2.divNew(n - 1);
			Element<?> stddev = variance.asDouble();
			for (ScalarElement e : stddev.getComponents()) {
				e.set(Math.sqrt(e.doubleValue()));
			}
			return stddev;
		}

		@Override
		public String toString() {
			return String.format("min: %s, max: %s, mean: %s, stddev: %s",
					min, max, mean, getStdDev());
		}

	}

}
