package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.query.stats.MockData.ParallelRand;

@RunWith(BlockJUnit4ClassRunner.class)
public class LedgerTest extends TestCase {

	final static int DIE_1_SIDES = 2000;
	final static int DIE_2_SIDES = 2000;
	final static double EPSILON = 1.0e-9;

	Logger log = LoggerFactory.getLogger(LedgerTest.class);

	@Test
	public void test_add() throws Exception {
		Ledger ledger = new Ledger();
		List<BucketingStrategy> bss = new ArrayList();
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		ledger.setBucketingStrategies(bss);

		ParallelRand generator = new ParallelRand(Arrays.asList(
			new double[] {0.0, 4.0},
			new double[] {0.0, 4.0},
			new double[] {0.0, 4.0}));

		for (int i = 0; i < 30; i++) {
			List<Double> pixel = generator.nextDoubles();
			ledger.add(pixel);
		}
		System.out.println(ledger.getCombinations().size());
		for (List<Double> key : ledger.getCombinations().keySet()) {
			long count = ledger.getCombinations().get(key);
			System.out.format("%s: %d\n", key, count);
		}
	}

}
