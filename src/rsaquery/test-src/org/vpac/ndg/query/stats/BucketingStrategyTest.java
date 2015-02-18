package org.vpac.ndg.query.stats;

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
		BucketingStrategyExplicit bs = new BucketingStrategyExplicit();
		bs.setBounds("30,30");
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
