package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.query.stats.MockData.ParallelRand;

@RunWith(BlockJUnit4ClassRunner.class)
public class LedgerTest extends TestCase {

	Logger log = LoggerFactory.getLogger(LedgerTest.class);

	@Test
	public void test_add_manual() throws Exception {
		Ledger ledger = new Ledger();
		List<BucketingStrategy> bss = new ArrayList<>();
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		ledger.setBucketingStrategies(bss);

		List<Double> pixel;
		ledger.add(Arrays.asList(0.0, 0.0, 0.0));
		ledger.add(Arrays.asList(0.0, 0.0, 1.0));
		ledger.add(Arrays.asList(0.0, 1.0, 2.0));
		ledger.add(Arrays.asList(1.0, 2.0, 3.0));
		ledger.add(Arrays.asList(0.0, 0.0, 0.0));
		ledger.add(Arrays.asList(0.0, 0.0, 1.0));
		ledger.add(Arrays.asList(0.0, 1.0, 2.0));
		ledger.add(Arrays.asList(0.0, 0.0, 0.0));
		ledger.add(Arrays.asList(0.0, 0.0, 1.0));
		ledger.add(Arrays.asList(0.0, 0.0, 0.0));

		assertEquals(4, ledger.get(Arrays.asList(0.0, 0.0, 0.0)));
		assertEquals(3, ledger.get(Arrays.asList(0.0, 0.0, 1.0)));
		assertEquals(2, ledger.get(Arrays.asList(0.0, 1.0, 2.0)));
		assertEquals(1, ledger.get(Arrays.asList(1.0, 2.0, 3.0)));
		assertEquals(0, ledger.get(Arrays.asList(5.0, 0.0, 0.0)));
	}

	@Test
	public void test_add() throws Exception {
		Ledger ledger = new Ledger();
		List<BucketingStrategy> bss = new ArrayList<>();
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		ledger.setBucketingStrategies(bss);

		ParallelRand generator = new ParallelRand(Arrays.asList(
			new double[] {0.0, 4.0},
			new double[] {0.0, 4.0},
			new double[] {0.0, 4.0}));

		int COUNT = 30;
		for (int i = 0; i < COUNT; i++) {
			List<Double> pixel = generator.nextDoubles();
			ledger.add(pixel);
		}
		log.info("Stored {} combinations in {}", COUNT, ledger);
		for (List<Double> key : ledger.keySet()) {
			long count = ledger.get(key);
			log.debug("{}: {}", key, count);
		}
	}

	/**
	 * Construct two ledgers in two ways with the same data: one serially, and
	 * the other in parallel. The parallel one is constructed as several
	 * separate ledger which are folded together at the end.
	 */
	@Test
	public void test_fold() throws Exception {
		Ledger ledger = new Ledger();
		Ledger[] partialLedgers = new Ledger[] {
			new Ledger(),
			new Ledger(),
			new Ledger(),
			new Ledger(),
		};
		List<BucketingStrategy> bss = new ArrayList<>();
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		bss.add(bf.create("regular/width/1"));
		ledger.setBucketingStrategies(bss);
		for (Ledger l : partialLedgers) {
			l.setBucketingStrategies(bss);
		}

		ParallelRand generator = new ParallelRand(Arrays.asList(
			new double[] {0.0, 5.0},
			new double[] {0.0, 5.0},
			new double[] {0.0, 5.0}));

		for (int i = 0; i < 400; i++) {
			List<Double> pixel = generator.nextDoubles();
			ledger.add(pixel);
			partialLedgers[i % partialLedgers.length].add(pixel);
		}

		Ledger foldedLedger = new Ledger();
		for (Ledger l : partialLedgers) {
			log.debug("Folding {}", l);
			foldedLedger = foldedLedger.fold(l);
		}

		log.info("Serial: {}, Folded: {}", ledger, foldedLedger);
		assertEquals(ledger.getCombinations(), foldedLedger.getCombinations());
	}

}
