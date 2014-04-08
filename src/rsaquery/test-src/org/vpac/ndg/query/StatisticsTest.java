package org.vpac.ndg.query;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.Statistics.Stats;
import org.vpac.ndg.query.math.ElementInt;

@RunWith(BlockJUnit4ClassRunner.class)
public class StatisticsTest extends TestCase {

	final static int DIE_1_SIDES = 2000;
	final static int DIE_2_SIDES = 2000;
	final static double EPSILON = 1.0e-9;

	Logger log = LoggerFactory.getLogger(StatisticsTest.class);

	private List<Integer> getPermutations(int n, int m) {
		List<Integer> permutations = new ArrayList<Integer>();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				permutations.add(i + j);
			}
		}
		return permutations;
	}

	/**
	 * Tests online statistics gathering, using the cheaper incremental method.
	 */
	@Test
	public void test_stats() throws Exception {
		List<Integer> permutations = getPermutations(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Stats test. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = mean(permutations);
		double sdev = stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		ElementInt value = new ElementInt();
		Stats stats = new Stats(value);
		for (Integer p : permutations) {
			value.set(p);
			stats.update(value);
		}
		log.info("Computed mean: {}, StdDev: {}",
				stats.mean.getComponents()[0].doubleValue(),
				stats.getStdDev().getComponents()[0].doubleValue());

		assertEquals(m, stats.mean.getComponents()[0].doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().getComponents()[0].doubleValue(), EPSILON);
	}

	/**
	 * Tests online statistics gathering, using the more expensive folding
	 * method. Each fold is performed in an incremental way, adding one datum
	 * each time. This tests the "small n" code path.
	 */
	@Test
	public void test_statsParallelSmallChunk() throws Exception {
		List<Integer> permutations = getPermutations(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Stats test. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = mean(permutations);
		double sdev = stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		ElementInt value = new ElementInt();
		Stats stats = null;
		Stats stats2;
		for (Integer p : permutations) {
			value.set(p);
			stats2 = new Stats(value);
			stats2.update(value);
			if (stats == null)
				stats = stats2;
			else
				stats = stats.fold(stats2);
		}
		log.info("Computed mean: {}, StdDev: {}",
				stats.mean.getComponents()[0].doubleValue(),
				stats.getStdDev().getComponents()[0].doubleValue());

		assertEquals(m, stats.mean.getComponents()[0].doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().getComponents()[0].doubleValue(), EPSILON);
	}

	/**
	 * Tests online statistics gathering, using the more expensive folding
	 * method. Each fold is performed on a chunk of data in a hierarchical
	 * reduction. This tests the "large n" code path.
	 */
	@Test
	public void test_statsParallel() throws Exception {
		List<Integer> permutations = getPermutations(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Stats test. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = mean(permutations);
		double sdev = stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		Stats stats = foldStatsRecursively(permutations);
		log.info("Computed mean: {}, StdDev: {}",
				stats.mean.getComponents()[0].doubleValue(),
				stats.getStdDev().getComponents()[0].doubleValue());

		assertEquals(m, stats.mean.getComponents()[0].doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().getComponents()[0].doubleValue(), EPSILON);
	}

	private Stats foldStatsRecursively(List<Integer> permutations) {
		if (permutations.size() <= 20)
			return calculateStatsIteratively(permutations);

		Stats left = foldStatsRecursively(permutations.subList(
				0, permutations.size() / 2));
		Stats right = foldStatsRecursively(permutations.subList(
				permutations.size() / 2, permutations.size()));
		return left.fold(right);
	}

	private Stats calculateStatsIteratively(List<Integer> permutations) {
		ElementInt value = new ElementInt();
		Stats stats = new Stats(value);
		for (Integer p : permutations) {
			value.set(p);
			stats.update(value);
		}
		return stats;
	}

	private double mean(List<Integer> xs) {
		long m = 0;
		for (Integer x : xs) {
			m += x;
		}
		return (double)m / xs.size();
	}

	/**
	 * Find the standard deviation of a list of integers. This uses a fool-proof
	 * two-pass algorithm.
	 * http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Two-pass_algorithm
	 */
	private double stddev(List<Integer> xs) {
		double m = mean(xs);
		double variance = 0.0;
		for (Integer x : xs) {
			variance += (x - m) * (x - m);
		}
		variance /= xs.size() - 1;
		return Math.sqrt(variance);
	}

}
