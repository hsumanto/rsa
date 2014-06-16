package org.vpac.ndg.query.stats;

import java.io.Serializable;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Basic statistics (min, max, mean, standard deviation).
 * @author Alex Fraser
 */
public class Stats implements Foldable<Stats>, Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private double min;
	private double max;
	private double mean;
	private long n;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getN() {
		return n;
	}

	public void setN(long n) {
		this.n = n;
	}

	public double getM2() {
		return M2;
	}

	public void setM2(double m2) {
		M2 = m2;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	// M2 = variance * (n - 1)
	// Variance is the square of the standard deviation.
	double M2;

	public Stats() {
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;

		n = 0;
		M2 = 0.0;
		mean = 0.0;
	}

	public void update(ScalarElement value) {
		if (!value.isValid())
			return;

		double v = value.doubleValue();
		min = Math.min(min, v);
		max = Math.max(max, v);

		// We only get one pass, so we have to use an online algorithm for
		// finding the variance. This is converted to the standard
		// deviation in getStdDev(). This algorithm is due to Knuth.
		n += 1;
		double delta = v - mean;
		double deltaProportional = delta / n;
		mean += deltaProportional;
		M2 += delta * (v - mean);
	}

	@Override
	public Stats fold(Stats other) {
		Stats res = new Stats();

		// nX = nA + nB
		res.n = n + other.n;
		if (res.n == 0)
			return res;

		res.min = Math.min(min, other.min);
		res.max = Math.max(max, other.max);

		// Algorithm for combining parallel-processed variance is due to
		// Chan et. al. with modification for stability suggested on
		// Wikipedia.
		// http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm

		double deltaWeight;
		double delta;
		double deltaSq;

		// delta = meanB - meanA
		delta = other.mean - mean;

		if (other.n < 10 || other.n < n / 10) {
			// For small n, or where nB is much smaller than nA.
			// meanX = meanA + delta * nB/nX
			deltaWeight = (double)other.n / (double)res.n;
			res.mean = mean + (delta * deltaWeight);
		} else {
			// For large n.
			// meanX = ((nA * meanA) + (nB * meanB)) / (nA + nB)
			res.mean = ((mean * n) + (other.mean * other.n)) / (double)res.n;
		}
		// (meanB - meanA)^2
		deltaSq = delta * delta;

		deltaWeight = ((double)n * (double)other.n) / (double)res.n;
		res.M2 = M2 + other.M2 + (deltaSq * deltaWeight);

		// The delta fields are re-calculated for each input, so we don't
		// need to fold them in.

		return res;
	}

	public long getCount() {
		return n;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getMean() {
		return mean;
	}

	public double getStdDev() {
		double variance = M2 / (n - 1);
		return Math.sqrt(variance);
	}

	@Override
	public String toString() {
		return String.format("min: %s, max: %s, mean: %s, stddev: %s",
				min, max, mean, getStdDev());
	}

}