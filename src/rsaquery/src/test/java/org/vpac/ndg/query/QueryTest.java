/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.ndg.query;

import java.io.File;
import java.io.IOException;
import java.lang.Comparable;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.query.stats.Stats;
import org.vpac.ndg.query.stats.VectorCats;
import org.vpac.ndg.query.stats.VectorHist;
import org.vpac.ndg.query.stats.VectorStats;
import org.vpac.ndg.query.testfilters.BrokenInheritanceFilter;
import org.vpac.ndg.query.testfilters.InheritanceFilter;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

@RunWith(BlockJUnit4ClassRunner.class)
public class QueryTest extends TestCase {

	Logger log = LoggerFactory.getLogger(QueryTest.class);

	//final private Logger log = LoggerFactory.getLogger(QueryTest.class);
	final int DEFAULT_PIXEL_NUMBER = 64;

	@Test(expected=IOException.class)
	public void test_invalid_href() throws Exception {
		File config = new File("data/config/invalid_href.xml");
		File outputFile = new File("data/output/invalid1.nc");

		QueryRunner.run(config, outputFile);
	}

	@Test
	public void test_1_wetting() throws Exception {
		File config = new File("data/config/wettingextents.xml");
		File outputFile = new File("data/output/watermark.nc");
		File expectedFile = new File("data/expected/watermark.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			Variable var = dataset.findVariable("wet");
			assertEquals("y x", var.getDimensionsString());
			assertEquals(64, var.getShape()[0]);
			assertEquals(64, var.getShape()[1]);

			Array output = var.read();
			Index ima = output.getIndex();
			Array outtime = dataset.findVariable("time").read();

			// Input is 3 linear gradients:
			//  - time=50:  east-west (right is low, top is high)
			//  - time=100: south-north (bottom is low, top is high)
			//  - time=150: west-east (left is low, right is high)
			// Output is a box, with its right (west) edge made of pixels from
			// time=50; the bottom edge from time=100, and the left edge from
			// time=150. There is no top edge.
			// Note that y increases going north.

			// Northern pixel, just outside the right box edge.
			ima.set(63, 47);
			assertEquals(0, output.getInt(ima));
			assertEquals(0, outtime.getInt(ima));
			// Just inside right box edge; matches t=50.
			ima.set(63, 48);
			assertEquals(1, output.getInt(ima));
			assertEquals(50, outtime.getInt(ima));
			// Level with bottom edge, but still inside right edge.
			ima.set(15, 48);
			assertEquals(1, output.getInt(ima));
			assertEquals(50, outtime.getInt(ima));
			// Just to the left of the right edge; matches t=100.
			ima.set(15, 47);
			assertEquals(1, output.getInt(ima));
			assertEquals(100, outtime.getInt(ima));
			// Level with left edge, but still inside bottom edge.
			ima.set(15, 15);
			assertEquals(1, output.getInt(ima));
			assertEquals(100, outtime.getInt(ima));
			// Just above bottom edge; matches t=150
			ima.set(16, 15);
			assertEquals(1, output.getInt(ima));
			assertEquals(150, outtime.getInt(ima));
			// Back up to the top, still inside left edge.
			ima.set(63, 15);
			assertEquals(1, output.getInt(ima));
			assertEquals(150, outtime.getInt(ima));

			// Semantic tests complete; now test every single cell, just for
			// fun!
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("wet");
			vac = dataset.findVariable("wet");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_2_fire() throws Exception {
		File config = new File("data/config/activefire.xml");
		File outputFile = new File("data/output/on_fire.nc");
		File expectedFile = new File("data/expected/on_fire.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			Variable var = dataset.findVariable("temp");
			assertEquals("y x", var.getDimensionsString());
			assertEquals(64, var.getShape()[0]);
			assertEquals(64, var.getShape()[1]);

			Array output = var.read();
			Index ima = output.getIndex();
			Array outtime = dataset.findVariable("time").read();

			// Input is 3 linear gradients:
			//  - time=50:  east-west (right is low, top is high)
			//  - time=100: south-north (bottom is low, top is high)
			//  - time=150: west-east (left is low, right is high)
			// Output is a box, with its right (west) edge made of pixels from
			// time=150; the top edge from time=100, and the left edge from
			// time=50. There is no bottom edge. Unlike the water test above,
			// this box has an angled interface where two edges meet.
			// Note that y increases going north.

			// Top-left pixel, just inside left edge.
			ima.set(63, 0);
			assertEquals(251, output.getInt(ima));
			assertEquals(50, outtime.getInt(ima));
			// Just outside left edge.
			ima.set(63, 1);
			assertEquals(251, output.getInt(ima));
			assertEquals(100, outtime.getInt(ima));
			// Top-right pixel, just inside top edge.
			ima.set(63, 63);
			assertEquals(251, output.getInt(ima));
			assertEquals(100, outtime.getInt(ima));
			// Just outside top edge.
			ima.set(62, 63);
			assertEquals(251, output.getInt(ima));
			assertEquals(150, outtime.getInt(ima));
			ima.set(49, 50);
			assertEquals(199, output.getInt(ima));
			assertEquals(150, outtime.getInt(ima));
			// Back in top edge - demonstrates angled nature of interface.
			ima.set(50, 49);
			assertEquals(199, output.getInt(ima));
			assertEquals(100, outtime.getInt(ima));
			// Just inside box (not in any edge).
			ima.set(49, 49);
			assertEquals(0, output.getInt(ima));
			assertEquals(0, outtime.getInt(ima));

			// Semantic tests complete; now test every single cell, just for
			// fun!
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("temp");
			vac = dataset.findVariable("temp");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_3_quality() throws Exception {
		File config = new File("data/config/qualityselection.xml");
		File outputFile = new File("data/output/quality_colour.nc");
		File expectedFile = new File("data/expected/quality_colour.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Red");
			vac = dataset.findVariable("colour1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("Green");
			vac = dataset.findVariable("colour2");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("Blue");
			vac = dataset.findVariable("colour3");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("Quality");
			vac = dataset.findVariable("quality");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	// There used to be two tests: 5a and 5b, which produced a graphical plot or
	// time series data (1D). However this functionality is now provided by the
	// plot web service in the RSA's Spatial Cube Service.

	@Test
	public void test_6a_minimiseVariance_onepass() throws Exception {
		File config = new File("data/config/minimisevariance.xml");
		File outputFile = new File("data/output/minvariance.nc");
		File expectedFile = new File("data/expected/minvariance.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			Variable var = dataset.findVariable("Band1");
			byte[] data = new byte[] {
					-110, -107, -103, -91, -92, -100,
				    -123, -113, -97,  -92, -91,  -84,
				    -106, -105, -91,  -80, -92, -100,
				     -95, -104, -90,  -91, -96, -103,
				     -98,  -95, -101, -95, -98,  -95,
				    -101, -106, -95,  -89, -92, -106};
			assertArray(data, var.read("0:5,0:5"));

			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Band1");
			vac = dataset.findVariable("Band1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_6b_minimiseVariance_twopass() throws Exception {
		File config = new File("data/config/minimisevariance_twopass.xml");
		File outputFile = new File("data/output/minvariance_twopass.nc");
		File expectedFile = new File("data/expected/minvariance_twopass.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			Variable var = dataset.findVariable("Band1");
			byte[] data = new byte[] {
					-110, -107, -103, -91, -92, -100,
				    -123, -113, -97,  -92, -91,  -84,
				    -106, -105, -91,  -80, -92, -100,
				     -95, -104, -90,  -91, -96, -103,
				     -98,  -95, -101, -95, -98,  -95,
				    -101, -106, -95,  -89, -92, -106};
			assertArray(data, var.read("0:5,0:5"));

			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Band1");
			vac = dataset.findVariable("Band1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_7_blur() throws Exception {
		File config = new File("data/config/blur.xml");
		File outputFile = new File("data/output/blur.nc");
		File expectedFile = new File("data/expected/blur.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile, 8);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Band1");
			vac = dataset.findVariable("Band1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_hypercube() throws Exception {
		File config = new File("data/config/hypercube.xml");
		File outputFile = new File("data/output/hypercube.nc");
		File expectedFile = new File("data/expected/hypercube.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Band1");
			vac = dataset.findVariable("Band1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_2D_2D() throws Exception {
		File config = new File("data/config/multiply_2d_2d.xml");
		File outputFile = new File("data/output/multiply_2d_2d.nc");
		File expectedFile = new File("data/expected/multiply_2d_2d.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("Band1");
			vac = dataset.findVariable("Band1");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_2D3D_demote() throws Exception {
		File config = new File("data/config/2d3d_demote.xml");
		File outputFile = new File("data/output/2d3d_demote.nc");
		File expectedFile = new File("data/expected/2d3d_demote.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("2dFirst");
			vac = dataset.findVariable("2dFirst");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("2dSecond");
			vac = dataset.findVariable("2dSecond");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_2D3D_promote() throws Exception {
		File config = new File("data/config/2d3d_promote.xml");
		File outputFile = new File("data/output/2d3d_promote.nc");
		File expectedFile = new File("data/expected/2d3d_promote.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("2dFirst");
			vac = dataset.findVariable("2dFirst");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("2dSecond");
			vac = dataset.findVariable("2dSecond");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test
	public void test_accumulate() throws Exception {
		File config = new File("data/config/accumulate.xml");
		File outputFile = new File("data/output/accumulate.nc");
		outputFile.delete();

		// Basic run with accumulate function. The output is assigned to the
		// map using the ID of the filter that accumulates it.
		Map<String, Foldable<?>> output = QueryRunner.run(config, outputFile);
		assertEquals("520240", output.get("sum").toString());

		// Run again with threading. This tests that the output can be folded
		// together.
		outputFile.delete();
		output = QueryRunner.run(config, outputFile, 8);
		assertEquals("520240", output.get("sum").toString());

	}

	final static double EPSILON = 1.0e-3;

	@Test
	public void test_statsFilter() throws Exception {
		File config = new File("data/config/stats_stats.xml");
		File outputFile = new File("data/output/stats.nc");
		outputFile.delete();

		Map<String, Foldable<?>> output = QueryRunner.run(config, outputFile, 8);
		VectorStats stats = (VectorStats) output.get("stats");

		assertEquals(35, stats.getMin()[0], EPSILON);
		assertEquals(230, stats.getMax()[0], EPSILON);
		assertEquals(124.04589843750001, stats.getMean()[0], EPSILON);
		assertEquals(31.17135124667003, stats.getStdDev()[0], EPSILON);
	}

	@Test
	public void test_histFilter() throws Exception {
		File config = new File("data/config/stats_hist.xml");
		File outputFile = new File("data/output/hist.nc");
		outputFile.delete();

		Map<String, Foldable<?>> output = QueryRunner.run(config, outputFile, 8);
		VectorHist vhist = (VectorHist) output.get("hist");

		Hist hist = vhist.getComponents()[0];
		List<Bucket> buckets = hist.optimise().getBuckets();
		Bucket b = buckets.get(0);
		Stats s = b.getStats();
		assertEquals("Number of elements in first bucket", 68, s.getCount());
		assertEquals("Mean of first bucket", 41.9706, s.getMean(), EPSILON);
		b = buckets.get(buckets.size() - 1);
		s = b.getStats();
		assertEquals("Number of elements in last bucket", 21, s.getCount());
		assertEquals("Mean of last bucket", 222.333, s.getMean(), EPSILON);
	}

	@Test
	public void test_catsFilter() throws Exception {
		File config = new File("data/config/stats_cats.xml");
		File outputFile = new File("data/output/cats.nc");
		outputFile.delete();

		Map<String, Foldable<?>> output = QueryRunner.run(config, outputFile, 8);
		VectorCats vcats = (VectorCats) output.get("cats");

		System.out.println(vcats.toString());

		Cats cats;
		Hist hist;
		List<Bucket> buckets;
		Bucket b;
		Stats s;

		cats = vcats.getComponents()[0];
		hist = cats.get(0);
		buckets = hist.optimise().getBuckets();
		b = buckets.get(0);
		s = b.getStats();
		assertEquals("Number of pixels in first bucket of category 0", 68, s.getCount());
		s = hist.summarise();
		assertEquals("Number of pixels where x < 64", 365, s.getCount());

		hist = cats.get(1);
		buckets = hist.optimise().getBuckets();
		b = buckets.get(0);
		s = b.getStats();
		assertEquals("Number of pixels in first bucket of category 1", 2233, s.getCount());
		s = hist.summarise();
		assertEquals("Number of pixels where 64 <= x < 128", 6422, s.getCount());

		hist = cats.get(2);
		s = hist.summarise();
		assertEquals("Number of pixels where 196 <= x", 5393, s.getCount());
	}

	@Test
	public void test_accountantFilter() throws Exception {
		File config = new File("data/config/stats_accountant.xml");
		File outputFile = new File("data/output/accountant.nc");
		outputFile.delete();

		Map<String, Foldable<?>> output = QueryRunner.run(config, outputFile, 8);
		Ledger ledger = (Ledger) output.get("accountant");
		log.info("Accountant found: {} ({})", ledger, ledger.getBucketingStrategies());
		List<List<Double>> keys = new ArrayList<>();
		keys.addAll(ledger.keySet());
		keys.sort(new ListComp<Double>());
		for (List<Double> key : keys) {
			log.debug("{}: {}", key, ledger.get(key));
		}
		assertEquals(30, ledger.size());
		assertEquals(1182, ledger.get(Arrays.asList(0.0, 255.0, 255.0, 120.0)));
	}

	/**
	 * Pairwise comparison of lists.
	 */
	public static class ListComp<T extends Comparable<T>>
			implements Comparator<List<T>> {
		@Override
		public int compare(List<T> a, List<T> b) {
			Comparator<T> natural = Comparator.<T>naturalOrder();
			int len = Math.min(a.size(), b.size());
			for (int i = 0; i < len; i++) {
				if (a.get(i) == null && b.get(i) == null)
					return 0;
				else if (a.get(i) == null)
					return -1;
				else if (b.get(i) == null)
					return 1;
				int ord = natural.compare(a.get(i), b.get(i));
				if (ord != 0)
					return ord;
			}
			return Integer.compare(a.size(), b.size());
		}
	}

	@Test
	public void test_inheritance() throws Exception {
		File config = new File("data/config/activefire.xml");
		File outputFile = new File("data/output/inheritance.nc");
		File expectedFile = new File("data/expected/on_fire.nc");
		outputFile.delete();

		QueryDefinition qd = QueryDefinition.fromXML(config);
		qd.filters.get(0).classname = InheritanceFilter.class.getName();
		File projectRoot = config.getParentFile();

		QueryRunner.run(qd, projectRoot, outputFile, 1);

		NetcdfFile dataset = null;
		NetcdfFile expected = null;
		try {
			dataset = NetcdfFile.open(outputFile.getPath());
			expected = NetcdfFile.open(expectedFile.getPath());
			Variable vex;
			Variable vac;
			vex = expected.findVariable("temp");
			vac = dataset.findVariable("temp");
			assertArray(vex.getDataType(), vex.read(), vac.read());
			vex = expected.findVariable("time");
			vac = dataset.findVariable("time");
			assertArray(vex.getDataType(), vex.read(), vac.read());
		} finally {
			if (dataset != null)
				dataset.close();
			if (expected != null)
				expected.close();
		}
	}

	@Test(expected=FilterDefinitionException.class)
	public void test_brokenInheritance() throws Exception {
		File config = new File("data/config/activefire.xml");
		File outputFile = new File("data/output/broken_inheritance.nc");
		outputFile.delete();

		QueryDefinition qd = QueryDefinition.fromXML(config);
		qd.filters.get(0).classname = BrokenInheritanceFilter.class.getName();
		File projectRoot = config.getParentFile();

		try {
			QueryRunner.run(qd, projectRoot, outputFile, 1);
		} catch (FilterDefinitionException e) {
			System.out.format("Exception received (good): %s\n", e.getMessage());
			throw e;
		}
	}

	@Test
	public void test_idsWithSpaces() throws Exception {
		File config = new File("data/config/ids_with_spaces.xml");

		QueryDefinition qd = QueryDefinition.fromXML(config);
		qd.filters.get(0).classname = BrokenInheritanceFilter.class.getName();

		File outputFile = new File("data/output/on_fire.nc");
		outputFile.delete();

		QueryRunner.run(config, outputFile);
	}


	/**
	 * Confirm that the contents of two arrays are the same.
	 */
	protected static void assertArray(byte[] expected, Array actual) {
		assertEquals(expected.length, actual.getSize());
		for (int i = 0; i < expected.length; i++) {
			byte ex = expected[i];
			byte ac = actual.getByte(i);
			assertEquals(ex, ac);
		}
	}

	/**
	 * Confirm that the contents of two arrays are the same.
	 */
	protected static void assertArray(int[] expected, Array actual) {
		assertEquals(expected.length, actual.getSize());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual.getInt(i));
		}
	}

	private void assertArray(DataType type, Array expected, Array actual) {
		assertEquals(expected.getSize(), actual.getSize());
		switch (type) {
		case BYTE:
		case SHORT:
		case INT:
		case LONG:
			for (int i = 0; i < expected.getSize(); i++) {
				long ex = expected.getLong(i);
				long ac = actual.getLong(i);
				assertEquals(ex, ac);
			}
			break;
		case FLOAT:
		case DOUBLE:
			for (int i = 0; i < expected.getSize(); i++) {
				double ex = expected.getDouble(i);
				double ac = actual.getDouble(i);
				assertEquals(ex, ac, 0.01);
			}
			break;
		default:
			throw new IllegalArgumentException(String.format(
					"Unsupported data type for comparison: %s", type));
		}
	}
}
