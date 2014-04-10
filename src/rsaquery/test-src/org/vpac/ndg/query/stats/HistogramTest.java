package org.vpac.ndg.query.stats;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(BlockJUnit4ClassRunner.class)
public class HistogramTest extends TestCase {

	Logger log = LoggerFactory.getLogger(HistogramTest.class);

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;
	static final int NUM_BUCKETS = 45;

	@Test
	public void test_lowerBoundGeneration() throws Exception {
		double lb;

		lb = Hist.lowerBound(0, BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE,
				SCALE);
		assertEquals(0.1, lb, 0.00001);

		lb = Hist.lowerBound(9, BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE,
				SCALE);
		assertEquals(100, lb, 0.00001);

		double[] lbs = Hist.genBuckets(BASE,
				BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE, NUM_BUCKETS);
		assertEquals(Double.NEGATIVE_INFINITY, lbs[0], 0.00001);
		assertEquals(0.0, lbs[22], 0.00001);
		assertEquals(1000000.0, lbs[44], 0.00001);
	}

}
