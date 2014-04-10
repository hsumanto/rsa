package org.vpac.ndg.query.stats;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Basic statistics (min, max, mean, standard deviation).
 * @author Alex Fraser
 */
public class Stats implements Foldable<Stats> {

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

	public void update(Element<?> value) {
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