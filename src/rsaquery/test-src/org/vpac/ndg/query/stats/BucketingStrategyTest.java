package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.QueryException;

@RunWith(BlockJUnit4ClassRunner.class)
public class BucketingStrategyTest extends TestCase {

	Logger log = LoggerFactory.getLogger(BucketingStrategyTest.class);

	static final double BASE = 10;
	static final double BUCKETS_PER_ORDER_OF_MAGNITUDE = 3.0;
	static final double SCALE = 0.1;

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
	public void test_logBoundsPrecision() throws Exception {
		BucketingStrategy bs;
		double[] boundaries;
		BucketingStrategyFactory factory = new BucketingStrategyFactory();

		bs = factory.create("log/base/10/n/5/scale/0.1");
		boundaries = new double[] {-10, -1, 0, 1, 10, 100};
		checkContiguity(bs, boundaries);
	}

	/**
	 * Checks that buckets are contiguous around a set of known boundaries.
	 * @param bs
	 * @param boundaries
	 */
	private void checkContiguity(BucketingStrategy bs, double[] boundaries) {
		for (double boundary : boundaries) {
			List<Double> derivedBoundaries = new ArrayList<Double>();

			double[] values = getDoublesAroundPoint(boundary, 1000);
			for (double value : values) {
				double[] bucket = bs.computeBucketBounds(value);
				if (!derivedBoundaries.contains(bucket[0]))
					derivedBoundaries.add(bucket[0]);
				if (!derivedBoundaries.contains(bucket[1]))
					derivedBoundaries.add(bucket[1]);

				if (Double.isNaN(bucket[0])) {
					bs.computeBucketBounds(value);
					throw new AssertionFailedError(String.format(
						"Lower bound of bucket is NaN! Value: %.24f",
						value));
				}

				if (Double.isNaN(bucket[1])) {
					bs.computeBucketBounds(value);
					throw new AssertionFailedError(String.format(
						"Upper bound of bucket is NaN! Value: %.24f",
						value));
				}

				if (bucket[0] > value || bucket[1] <= value) {
					bs.computeBucketBounds(value);
					throw new AssertionFailedError(String.format(
						"Bucket [%.24f, %.24f] doesn't contain value %.24f",
						bucket[0], bucket[1], value));
				}
			}

			log.debug("Derived bounds near {}: {}",
					boundary, derivedBoundaries);

			log.debug("Tested from {} to {}", values[0], values[values.length - 1]);
			assertTrue("Buckets are non-contiguous",
					derivedBoundaries.size() == 3);
		}
	}

	private double[] getDoublesAroundPoint(double point, int n) {
		double[] values = new double[(n * 2) + 1];

		double start = point;
		for (int i = n; i >= 0; i--) {
			start = Math.nextAfter(start, Double.NEGATIVE_INFINITY);
			values[i] = start;
		}

		values[n] = point;

		double end = point;
		for (int i = n + 1; i < values.length; i++) {
			end = Math.nextAfter(end, Double.POSITIVE_INFINITY);
			values[i] = end;
		}

		return values;
	}

	@Test
	public void test_falseLogSequence() throws Exception {
		BucketingStrategyLog bs = new BucketingStrategyLogRegular();

		bs.setBase(10);
		bs.setN(5);
		bs.setScale(0.1);

		double[] bounds = new double[20];
		for (int i = 0; i < bounds.length; i++) {
			bounds[i] = bs.lowerBound(i);
		}
		log.debug("Bounds: {}", bounds);
		for (int i = 0; i < bounds.length - 1; i++) {
			double delta = bounds[i + 1] - bounds[i];
			assertTrue("Buckets are not monotonitcally increasing", delta > 0);
			if (delta < 0.05) {
				throw new AssertionFailedError(String.format(
					"Bucket %d (lb %f) is ridiculously small", i, bounds[i]));
			}
		}
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
		BucketingStrategyFactory factory = new BucketingStrategyFactory();
		String descriptor = String.format("logRegular?base=%g&n=%g&scale=%g",
				BASE, BUCKETS_PER_ORDER_OF_MAGNITUDE, SCALE);
		BucketingStrategy bs = factory.create(descriptor);

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
	 * Make sure that:
	 *
	 * <ol>
	 * <li>Buckets contain the value that was requested.</li>
	 * <li>Buckets are perfectly contiguous (always touch, never overlap).</li>
	 * </ol>
	 */
	@Test
	public void test_logRegularBoundsPrecision() throws Exception {
		BucketingStrategy bs;
		double[] boundaries;
		BucketingStrategyFactory factory = new BucketingStrategyFactory();

		bs = factory.create("logRegular/base/10/n/5/scale/0.1");
		boundaries = new double[] {
				-0.8, -0.6, -0.4, -0.2, -0.1,
				0,
				0.1, 0.2, 0.4, 0.6, 0.8,
				1, 2, 4, 6, 8,
				10, 20, 40, 60, 80};
		checkContiguity(bs, boundaries);
	}

	@Test(expected=QueryException.class)
	public void test_logRegularBadNumBuckets() throws Exception {
		BucketingStrategyFactory factory = new BucketingStrategyFactory();

		factory.create("logRegular/base/10/n/10/scale/10");
//		boundaries = new double[] {-20, -10, 0, 10, 20, 40, 60, 80, 100, 110};
//		checkContiguity(bs, boundaries);
	}

	@Test
	public void test_explicit() throws Exception {
		double[] bucket;

		BucketingStrategyExplicit bs = new BucketingStrategyExplicit();

		bs.setBounds("10,20,30");
		bucket = bs.computeBucketBounds(0);
		assertEquals(Double.NEGATIVE_INFINITY, bucket[0]);
		assertEquals(10.0, bucket[1]);
		bucket = bs.computeBucketBounds(10.0);
		assertEquals(10.0, bucket[0]);
		assertEquals(20.0, bucket[1]);
		bucket = bs.computeBucketBounds(20.0);
		assertEquals(20.0, bucket[0]);
		assertEquals(30.0, bucket[1]);
		bucket = bs.computeBucketBounds(30.0);
		assertEquals(20.0, bucket[0]);
		assertEquals(30.0, bucket[1]);
		bucket = bs.computeBucketBounds(31.0);
		assertEquals(30.0, bucket[0]);
		assertEquals(Double.POSITIVE_INFINITY, bucket[1]);

		bs.setBounds("5.0");
		bucket = bs.computeBucketBounds(0);
		assertEquals(Double.NEGATIVE_INFINITY, bucket[0]);
		assertEquals(5.0, bucket[1]);
		bucket = bs.computeBucketBounds(5);
		assertEquals(Double.NEGATIVE_INFINITY, bucket[0]);
		assertEquals(5.0, bucket[1]);
		bucket = bs.computeBucketBounds(6);
		assertEquals(5.0, bucket[0]);
		assertEquals(Double.POSITIVE_INFINITY, bucket[1]);

		bs.setBounds("");
		bucket = bs.computeBucketBounds(0);
		assertEquals(Double.NEGATIVE_INFINITY, bucket[0]);
		assertEquals(Double.POSITIVE_INFINITY, bucket[1]);
	}

	@Test(expected=QueryException.class)
	public void test_explicitNonIncreasing() throws Exception {
		BucketingStrategyFactory factory = new BucketingStrategyFactory();
		factory.create("explicit/bounds/30,30");
	}

	@Test
	public void test_explicitPrecision() throws Exception {
		BucketingStrategy bs;
		double[] boundaries;
		BucketingStrategyFactory factory = new BucketingStrategyFactory();

		bs = factory.create("explicit/bounds/-3, -0.1, 0, 0.3, 5, 5000, Infinity");
		boundaries = new double[] {-3, -0.1, 0, 0.3, 5, 5000};
		checkContiguity(bs, boundaries);
	}

	@Test
	public void test_regular() throws Exception {
		double[] bucket;

		BucketingStrategyRegular bs = new BucketingStrategyRegular();

		bs.width = 10.0;
		bucket = bs.computeBucketBounds(-101);
		assertEquals(-110.0, bucket[0], EPSILON);
		assertEquals(-100.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(-0.0001);
		assertEquals(-10.0, bucket[0], EPSILON);
		assertEquals(0.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(0);
		assertEquals(0.0, bucket[0], EPSILON);
		assertEquals(10.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(5);
		assertEquals(0.0, bucket[0], EPSILON);
		assertEquals(10.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(99.99);
		assertEquals(90.0, bucket[0], EPSILON);
		assertEquals(100.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(100);
		assertEquals(100.0, bucket[0], EPSILON);
		assertEquals(110.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(1000000000000.0);
		assertEquals(1000000000000.0, bucket[0], EPSILON);
		assertEquals(1000000000010.0, bucket[1], EPSILON);

		bs.origin = 5.0;
		bucket = bs.computeBucketBounds(-6);
		assertEquals(-15.0, bucket[0], EPSILON);
		assertEquals(-5.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(0);
		assertEquals(-5.0, bucket[0], EPSILON);
		assertEquals(5.0, bucket[1], EPSILON);
		bucket = bs.computeBucketBounds(6);
		assertEquals(5.0, bucket[0], EPSILON);
		assertEquals(15.0, bucket[1], EPSILON);
	}

	@Test
	public void test_regularPrecision() throws Exception {
		BucketingStrategy bs;
		double[] boundaries;
		BucketingStrategyFactory factory = new BucketingStrategyFactory();

		bs = factory.create("regular/origin/5/width/10");
		boundaries = new double[] {-5, 5, 15, 25, 105, 5005};
		checkContiguity(bs, boundaries);
	}

	@Test
	public void test_urlTypes() throws Exception {
		String descriptor;
		BucketingStrategyLog bs;

		descriptor = String.format("log?base=10&n=3&scale=0.1");
		bs = (BucketingStrategyLog) new BucketingStrategyFactory().create(descriptor);
		assertEquals(10.0, bs.base);
		assertEquals(3.0, bs.n);
		assertEquals(0.1, bs.scale);

		descriptor = String.format("log/base/10/n/3/scale/0.1");
		bs = (BucketingStrategyLog) new BucketingStrategyFactory().create(descriptor);
		assertEquals(10.0, bs.base);
		assertEquals(3.0, bs.n);
		assertEquals(0.1, bs.scale);
	}

}
