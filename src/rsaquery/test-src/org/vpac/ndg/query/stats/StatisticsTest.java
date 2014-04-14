package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.iteration.ListTranslator;
import org.vpac.ndg.query.math.ElementInt;
import org.vpac.ndg.query.math.VectorElement;

@RunWith(BlockJUnit4ClassRunner.class)
public class StatisticsTest extends TestCase {

	final static int DIE_1_SIDES = 2000;
	final static int DIE_2_SIDES = 2000;
	final static double EPSILON = 1.0e-9;

	Logger log = LoggerFactory.getLogger(StatisticsTest.class);

	/**
	 * Tests online statistics gathering, using the cheaper incremental method.
	 */
	@Test
	public void test_stats() throws Exception {
		List<Integer> permutations = MockData.permute(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Iterative. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = MockData.mean(permutations);
		double sdev = MockData.stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		Stats stats = new Stats(new ElementInt());
		for (ElementInt value : ListTranslator.ints(permutations)) {
			stats.update(value);
		}
		log.info("Computed mean: {}, StdDev: {}", stats.mean, stats.getStdDev());

		assertEquals(m, stats.getMean().doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().doubleValue(), EPSILON);
	}

	/**
	 * Tests online statistics gathering, using the more expensive folding
	 * method. Each fold is performed in an incremental way, adding one datum
	 * each time. This tests the "small n" code path.
	 */
	@Test
	public void test_parallelSmallChunk() throws Exception {
		List<Integer> permutations = MockData.permute(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Parallel small. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = MockData.mean(permutations);
		double sdev = MockData.stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		Stats stats = null;
		Stats stats2;
		for (ElementInt value : ListTranslator.ints(permutations)) {
			stats2 = new Stats(value);
			stats2.update(value);
			if (stats == null)
				stats = stats2;
			else
				stats = stats.fold(stats2);
		}
		log.info("Computed mean: {}, StdDev: {}", stats.getMean(), stats.getStdDev());

		assertEquals(m, stats.getMean().doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().doubleValue(), EPSILON);
	}

	/**
	 * Tests online statistics gathering, using the more expensive folding
	 * method. Each fold is performed on a chunk of data in a hierarchical
	 * reduction. This tests the "large n" code path.
	 */
	@Test
	public void test_parallel() throws Exception {
		List<Integer> permutations = MockData.permute(DIE_1_SIDES, DIE_2_SIDES);
		log.info("Parallel large. Permutations: {}", permutations.size());

		// Calculate mean and stddev using a basic algorithm.
		double m = MockData.mean(permutations);
		double sdev = MockData.stddev(permutations);
		log.info("Baseline mean: {}, StdDev: {}", m, sdev);

		// Now compare the computation above to other algorithm being tested.
		Stats stats = foldStatsRecursively(permutations);
		log.info("Computed mean: {}, StdDev: {}", stats.getMean(), stats.getStdDev());

		assertEquals(m, stats.getMean().doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().doubleValue(), EPSILON);
	}

	/**
	 * Tests computation of vector statistics (i.e. simultaneous collection of
	 * stats from multiple input bands).
	 */
	@Test
	public void test_vector() throws Exception {
		List<Integer> permutations = MockData.permute(100, 100);
		log.info("Vector. Permutations: {}", permutations.size());

		List<VectorElement> inputs = new ArrayList<VectorElement>();
		for (Integer i : permutations) {
			VectorElement e = new VectorElement(
					new ElementInt(i / 10),
					new ElementInt(i),
					new ElementInt(i * 10));
			inputs.add(e);
		}

		// Calculate mean and stddev using a basic algorithm.
		double m = MockData.mean(permutations);
		double sdev = MockData.stddev(permutations);
		log.info("Middle (i=1) baseline mean: {}, StdDev: {}", m, sdev);

		VectorStats stats = foldVStatsRecursively(inputs);

		// Now compare the computation above to other algorithm being tested.
		log.info("Computed mean: {}, StdDev: {}", stats.getMean(), stats.getStdDev());

		assertEquals(m, stats.getMean().getComponents()[1].doubleValue(), EPSILON);
		assertEquals(sdev, stats.getStdDev().getComponents()[1].doubleValue(), EPSILON);
	}

	private Stats foldStatsRecursively(List<Integer> inputs) {
		if (inputs.size() <= 20)
			return calculateStatsIteratively(inputs);

		Stats left = foldStatsRecursively(inputs.subList(
				0, inputs.size() / 2));
		Stats right = foldStatsRecursively(inputs.subList(
				inputs.size() / 2, inputs.size()));
		return left.fold(right);
	}

	private Stats calculateStatsIteratively(List<Integer> inputs) {
		Stats stats = new Stats(new ElementInt());
		for (ElementInt value : ListTranslator.ints(inputs)) {
			stats.update(value);
		}
		return stats;
	}

	private VectorStats foldVStatsRecursively(List<VectorElement> inputs) {
		if (inputs.size() <= 20)
			return calculateVStatsIteratively(inputs);

		VectorStats left = foldVStatsRecursively(inputs.subList(
				0, inputs.size() / 2));
		VectorStats right = foldVStatsRecursively(inputs.subList(
				inputs.size() / 2, inputs.size()));
		return left.fold(right);
	}

	private VectorStats calculateVStatsIteratively(List<VectorElement> inputs) {
		VectorStats stats = new VectorStats(inputs.get(0));
		for (VectorElement value : inputs) {
			stats.update(value);
		}
		return stats;
	}

}
