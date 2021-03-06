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

	/**
	 * Add known values to a ledger and check the occurrences
	 */
	@Test
	public void test_add_manual() throws Exception {
		Ledger ledger = new Ledger();
		ledger.setBucketingStrategies(Arrays.asList(
			"regular/width/1",
			"regular/width/1",
			"regular/width/1"
		));

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

		assertEquals(10, ledger.totalCount());
		assertEquals(4, ledger.get(Arrays.asList(0.0, 0.0, 0.0)));
		assertEquals(3, ledger.get(Arrays.asList(0.0, 0.0, 1.0)));
		assertEquals(2, ledger.get(Arrays.asList(0.0, 1.0, 2.0)));
		assertEquals(1, ledger.get(Arrays.asList(1.0, 2.0, 3.0)));
		assertEquals(0, ledger.get(Arrays.asList(5.0, 0.0, 0.0)));
	}

	@Test
	public void test_add() throws Exception {
		Ledger ledger = new Ledger();
		ledger.setBucketingStrategies(Arrays.asList(
			"regular/width/1",
			"regular/width/1",
			"regular/width/1"
		));

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
		assertEquals(COUNT, ledger.totalCount());
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
		List<String> bss = Arrays.asList(
			"regular/width/1",
			"regular/width/1",
			"regular/width/1"
		);
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
		assertEquals(ledger.totalCount(), foldedLedger.totalCount());
		assertEquals(ledger.entrySet(), foldedLedger.entrySet());
	}

	/**
	 * Construct a ledger, then filter it by column. Check that the folded
	 * counts are sensible.
	 */
	@Test
	public void test_filter() throws Exception {
		Ledger ledger = new Ledger();
		ledger.setBucketingStrategies(Arrays.asList(
			"regular/width/1",
			"regular/width/1",
			"regular/width/1"
		));

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
		assertEquals("Ledger(3x4)", ledger.toString());
		assertEquals(10, ledger.totalCount());

		Ledger filtered;
		filtered = ledger.filterColumns(Arrays.asList(0, 1));
		assertEquals("Ledger(2x3)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(7, filtered.get(Arrays.asList(0.0, 0.0)));
		assertEquals(2, filtered.get(Arrays.asList(0.0, 1.0)));
		assertEquals(1, filtered.get(Arrays.asList(1.0, 2.0)));

		filtered = ledger.filterColumns(Arrays.asList(0, 2));
		assertEquals("Ledger(2x4)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(4, filtered.get(Arrays.asList(0.0, 0.0)));
		assertEquals(3, filtered.get(Arrays.asList(0.0, 1.0)));
		assertEquals(2, filtered.get(Arrays.asList(0.0, 2.0)));
		assertEquals(1, filtered.get(Arrays.asList(1.0, 3.0)));

		filtered = ledger.filterColumns(Arrays.asList(1, 2));
		assertEquals("Ledger(2x4)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(4, filtered.get(Arrays.asList(0.0, 0.0)));
		assertEquals(3, filtered.get(Arrays.asList(0.0, 1.0)));
		assertEquals(2, filtered.get(Arrays.asList(1.0, 2.0)));
		assertEquals(1, filtered.get(Arrays.asList(2.0, 3.0)));

		// Test reversed order
		filtered = ledger.filterColumns(Arrays.asList(2, 1));
		assertEquals("Ledger(2x4)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(4, filtered.get(Arrays.asList(0.0, 0.0)));
		assertEquals(3, filtered.get(Arrays.asList(1.0, 0.0)));
		assertEquals(2, filtered.get(Arrays.asList(2.0, 1.0)));
		assertEquals(1, filtered.get(Arrays.asList(3.0, 2.0)));

		filtered = ledger.filterColumns(Arrays.asList(0));
		assertEquals("Ledger(1x2)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(9, filtered.get(Arrays.asList(0.0)));
		assertEquals(1, filtered.get(Arrays.asList(1.0)));

		filtered = ledger.filterColumns(Arrays.asList(1));
		assertEquals("Ledger(1x3)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(7, filtered.get(Arrays.asList(0.0)));
		assertEquals(2, filtered.get(Arrays.asList(1.0)));
		assertEquals(1, filtered.get(Arrays.asList(2.0)));

		filtered = ledger.filterColumns(Arrays.asList(2));
		assertEquals("Ledger(1x4)", filtered.toString());
		assertEquals(10, filtered.totalCount());
		assertEquals(4, filtered.get(Arrays.asList(0.0)));
		assertEquals(3, filtered.get(Arrays.asList(1.0)));
		assertEquals(2, filtered.get(Arrays.asList(2.0)));
		assertEquals(1, filtered.get(Arrays.asList(3.0)));
	}

}
