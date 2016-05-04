package org.vpac.ndg.query.stats;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.iteration.Pair;
import org.vpac.ndg.query.math.ElementInt;

@RunWith(BlockJUnit4ClassRunner.class)
public class CategoriesTest extends TestCase {

	Logger log = LoggerFactory.getLogger(CategoriesTest.class);

	final static int DIE_1_SIDES = 1000;
	final static int DIE_2_SIDES = 2000;

	final static double EPSILON = 1.0e-9;

	private static final long NUM_ELEMENTS = 5000 * 5000;

	private List<Pair<Integer, Integer>> permutations;

	@Before
	public void setUp() {
		permutations = MockData.permutePairs(DIE_1_SIDES, DIE_2_SIDES);
	}

	@Test
	public void test_CategoriesPopulation() throws Exception {

		ElementInt category = new ElementInt();
		ElementInt value = new ElementInt();

		Cats cats = new Cats();
		cats.setBucketingStrategy(new BucketingStrategyLog());
		for (Pair<Integer, Integer> p : permutations) {
			category.set(p.a);
			value.set(p.a * p.b);
			cats.update(category, value);
		}

		Set<Entry<Integer, Hist>> entries = cats.getEntries();
		assertEquals("Number of categories", 1000, entries.size());

		Hist hist = cats.get(1).optimise();
		List<Bucket> buckets = hist.getBuckets();
		Bucket b = buckets.get(0);
		Stats s = b.getStats();
		assertEquals("Lower bound of first bucket of category 1", 1.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket of category 1", 2, s.getCount());
		assertEquals("Mean of first bucket of category 1", 1.5, s.getMean(), EPSILON);

		hist = cats.get(10).optimise();
		buckets = hist.getBuckets();
		b = buckets.get(0);
		s = b.getStats();
		assertEquals("Lower bound of first bucket of category 10", 10.0, b.getLower(), EPSILON);
		assertEquals("Elements in first bucket of category 10", 2, s.getCount());
		assertEquals("Mean of first bucket of category 10", 15.0, s.getMean(), EPSILON);
	}

	@Test
	public void test_WholeTile() throws Exception {

		ElementInt category = new ElementInt();
		ElementInt value = new ElementInt();

		int nperms = permutations.size();

		Cats cats = new Cats();
		cats.setBucketingStrategy(new BucketingStrategyLog());
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			Pair<Integer, Integer> p = permutations.get(i % nperms);
			category.set(p.a);
			value.set(p.a * p.b);
			cats.update(category, value);
		}

		Set<Entry<Integer, Hist>> entries = cats.getEntries();
		assertEquals("Number of categories", 1000, entries.size());

		Hist hist = cats.get(1).optimise();
		List<Bucket> buckets = hist.getBuckets();
		Bucket b = buckets.get(0);
		assertEquals("Lower bound of first bucket of category 1", 1.0, b.getLower(), EPSILON);

		hist = cats.get(10).optimise();
		buckets = hist.getBuckets();
		b = buckets.get(0);
		assertEquals("Lower bound of first bucket of category 10", 10.0, b.getLower(), EPSILON);
	}

}
