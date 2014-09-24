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
public class HistogramTest extends TestCase {

	Logger log = LoggerFactory.getLogger(HistogramTest.class);

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	final static int DIE_1_SIDES = 2000;
	final static int DIE_2_SIDES = 2000;

	final static double EPSILON = 1.0e-4;

	@Test
	public void test_logLowerBounds() throws Exception {
		double lb;
		int i;

		BucketingStrategyLog bs = new BucketingStrategyLog();
		bs.setBase(BASE);
		bs.setN(BUCKETS_PER_ORDER_OF_MAGNITUDE);
		bs.setScale(SCALE);

		i = bs.indexOf(0.1);
		assertEquals(0, i);
		lb = bs.lowerBound(0);
		assertEquals(0.1, lb, EPSILON);

		i = bs.indexOf(100);
		assertEquals(9, i);
		lb = bs.lowerBound(9);
		assertEquals(100, lb, EPSILON);
	}

	@Test
	public void test_logBounds() throws Exception {
		String descriptor = String.format("log?base=%g&n=%g&scale=%g",
				BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE);
		BucketingStrategy bs = new BucketingStrategyFactory().create(descriptor);

		double[] bounds;
		bounds = bs.computeBucketBounds(0.0);
		assertEquals(0.0, bounds[0], EPSILON);
		assertEquals(SCALE, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(SCALE / 2);
		assertEquals(0.0, bounds[0], EPSILON);
		assertEquals(SCALE, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(-SCALE / 2);
		assertEquals(-SCALE, bounds[0], EPSILON);
		assertEquals(0.0, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(5.0);
		assertEquals(4.6415888336, bounds[0], EPSILON);
		assertEquals(10.0, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(-5.0);
		assertEquals(-10.0, bounds[0], EPSILON);
		assertEquals(-4.6415888336, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.POSITIVE_INFINITY);
		assertEquals(Double.POSITIVE_INFINITY, bounds[0], EPSILON);
		assertEquals(Double.POSITIVE_INFINITY, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.NEGATIVE_INFINITY);
		assertEquals(Double.NEGATIVE_INFINITY, bounds[0], EPSILON);
		assertEquals(Double.NEGATIVE_INFINITY, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.NaN);
		assertEquals(Double.NaN, bounds[0], EPSILON);
		assertEquals(Double.NaN, bounds[1], EPSILON);

	}

	@Test
	public void test_falseLogLowerBounds() throws Exception {
		double lb;
		int i;

		BucketingStrategyLog bs = new BucketingStrategyLogRegular();
		bs.setBase(BASE);
		bs.setN(BUCKETS_PER_ORDER_OF_MAGNITUDE);
		bs.setScale(SCALE);

		i = bs.indexOf(0.1);
		assertEquals(0, i);
		lb = bs.lowerBound(0);
		assertEquals(0.1, lb, EPSILON);

		i = bs.indexOf(0.4);
		assertEquals(1, i);
		lb = bs.lowerBound(1);
		assertEquals(0.33333333333333, lb, EPSILON);

		i = bs.indexOf(40);
		assertEquals(7, i);
		lb = bs.lowerBound(7);
		assertEquals(33.33333333333, lb, EPSILON);

		i = bs.indexOf(100);
		assertEquals(9, i);
		lb = bs.lowerBound(9);
		assertEquals(100, lb, EPSILON);

		i = bs.indexOf(200);
		assertEquals(9, i);

		i = bs.indexOf(300);
		assertEquals(9, i);

		i = bs.indexOf(800);
		assertEquals(11, i);
		lb = bs.lowerBound(11);
		assertEquals(666.6666666667, lb, EPSILON);
	}

	@Test
	public void test_falseLogBounds() throws Exception {
		String descriptor = String.format("logRegular?base=%g&n=%g&scale=%g",
				BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE);
		BucketingStrategy bs = new BucketingStrategyFactory().create(descriptor);

		double[] bounds;
		bounds = bs.computeBucketBounds(0.0);
		assertEquals(0.0, bounds[0], EPSILON);
		assertEquals(SCALE, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(SCALE / 2);
		assertEquals(0.0, bounds[0], EPSILON);
		assertEquals(SCALE, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(-SCALE / 2);
		assertEquals(-SCALE, bounds[0], EPSILON);
		assertEquals(0.0, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(5.0);
		assertEquals(3.3333333333, bounds[0], EPSILON);
		assertEquals(6.6666666667, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(-5.0);
		assertEquals(-6.6666666667, bounds[0], EPSILON);
		assertEquals(-3.3333333333, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.POSITIVE_INFINITY);
		assertEquals(Double.POSITIVE_INFINITY, bounds[0], EPSILON);
		assertEquals(Double.POSITIVE_INFINITY, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.NEGATIVE_INFINITY);
		assertEquals(Double.NEGATIVE_INFINITY, bounds[0], EPSILON);
		assertEquals(Double.NEGATIVE_INFINITY, bounds[1], EPSILON);

		bounds = bs.computeBucketBounds(Double.NaN);
		assertEquals(Double.NaN, bounds[0], EPSILON);
		assertEquals(Double.NaN, bounds[1], EPSILON);

	}

	/**
	 * Basic scalar histogram.
	 */
	@Test
	public void test_population() throws Exception {
		List<Integer> permutations = MockData.permute(DIE_1_SIDES, DIE_2_SIDES);

		String descriptor = String.format("log?base=%g&n=%g&scale=%g",
				BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE);
		BucketingStrategy bs = new BucketingStrategyFactory().create(descriptor);
		Hist hist = new Hist();
		hist.setBucketingStrategy(bs);
		for (ElementInt value : ListTranslator.ints(permutations)) {
			hist.update(value);
		}
		log.info("Histogram: {}", hist.toCsv());

		Hist optimisedHist = hist.optimise();
		List<Bucket> buckets = optimisedHist.getBuckets();
		assertEquals("Number of nonempty buckets", 11, buckets.size());

		Bucket b = buckets.get(0);
		Stats s = b.getStats();
		assertEquals("Lower bound of first bucket", 1.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket", 1, s.getCount());
		assertEquals("Minimum of first bucket", 2, s.getMin(), EPSILON);
		assertEquals("Maximum of first bucket", 2, s.getMax(), EPSILON);
		assertEquals("Mean of first bucket", 2.0, s.getMean(), EPSILON);
		assertEquals("StdDev of first bucket", Double.NaN, s.getStdDev(), EPSILON);

		b = buckets.get(buckets.size() - 2);
		s = b.getStats();
		assertEquals("Lower bound of second-last bucket", 1000.0, b.getLower(), EPSILON);
		assertEquals("Elements in second-last bucket", 1796718, s.getCount());
		assertEquals("Minimum of second-last bucket", 1000, s.getMin(), EPSILON);
		assertEquals("Maximum of second-last bucket", 2154, s.getMax(), EPSILON);
		assertEquals("Mean of second-last bucket", 1641.5612411073373, s.getMean(), EPSILON);
		assertEquals("StdDev of second-last bucket", 323.7411898125304, s.getStdDev(), EPSILON);
	}

	/**
	 * Tests computation of vector statistics (i.e. simultaneous collection of
	 * histograms from multiple input bands).
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

		String descriptor = String.format("log?base=%g&n=%g&scale=%g",
				BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE);
		BucketingStrategy bs = new BucketingStrategyFactory().create(descriptor);
		VectorHist stats = foldVStatsRecursively(inputs, bs);
		log.info("Histogram: {}", stats);

		int numLessThanTen = 0;
		for (Integer i : permutations) {
			if (i < 10)
				numLessThanTen++;
		}

		Hist optimisedHist = stats.getComponents()[0].optimise();
		Bucket b = optimisedHist.getBuckets().get(0);
		Stats s = b.getStats();
		assertEquals("Lower bound of first bucket of first component", 0.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket of first component", numLessThanTen, s.getCount());

		optimisedHist = stats.getComponents()[1].optimise();
		b = optimisedHist.getBuckets().get(0);
		s = b.getStats();
		assertEquals("Lower bound of first bucket of second component", 1.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket of second component", 1, s.getCount());

		optimisedHist = stats.getComponents()[2].optimise();
		b = optimisedHist.getBuckets().get(0);
		s = b.getStats();
		assertEquals("Lower bound of first bucket of third component", 10.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket of third component", 1, s.getCount());
	}

	private VectorHist foldVStatsRecursively(List<VectorElement> inputs,
			BucketingStrategy bs) {
		if (inputs.size() <= 20)
			return calculateVStatsIteratively(inputs, bs);

		VectorHist left = foldVStatsRecursively(inputs.subList(
				0, inputs.size() / 2), bs);
		VectorHist right = foldVStatsRecursively(inputs.subList(
				inputs.size() / 2, inputs.size()), bs);
		return left.fold(right);
	}

	private VectorHist calculateVStatsIteratively(List<VectorElement> inputs,
			BucketingStrategy bs) {
		VectorHist hist = new VectorHist(inputs.get(0).size());
		hist.setBucketingStrategy(bs);
		for (VectorElement value : inputs) {
			hist.update(value);
		}
		return hist;
	}

}
