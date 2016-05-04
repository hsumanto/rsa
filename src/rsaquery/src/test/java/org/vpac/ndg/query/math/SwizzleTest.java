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

package org.vpac.ndg.query.math;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(BlockJUnit4ClassRunner.class)
public class SwizzleTest {

	@Test
	public void test_int() throws Exception {
		Swizzle sw = SwizzleFactory.compile("zxyx");
		VectorInt from = VectorInt.create(1, 2, 3);
		VectorInt to = VectorInt.createEmpty(4);
		VectorInt expected;

		sw.swizzle(from, to);
		expected = VectorInt.create(1, 3, 2, 3);
		assertEquals(expected, to);
	}

	@Test
	public void test_real() throws Exception {
		Swizzle sw = SwizzleFactory.compile("zxyx");
		VectorReal from = VectorReal.create(1, 2, 3);
		VectorReal to = VectorReal.createEmpty(4);
		VectorReal expected;

		sw.swizzle(from, to);
		expected = VectorReal.create(1, 3, 2, 3);
		assertEquals(expected, to);
	}

	@Test
	public void test_element() throws Exception {
		Swizzle sw = SwizzleFactory.compile("zxyx");
		VectorElement from = new VectorElement(
				new ElementFloat(1.5f),
				new ElementByte((byte)2),
				new ElementLong(10000000000000l)
				);
		// Note: type conversion to long
		VectorElement to = VectorElement.createInt(4, 0);

		VectorElement expected = new VectorElement(
				new ElementLong(1),
				new ElementLong(10000000000000l),
				new ElementLong(2),
				new ElementLong(10000000000000l)
				);

		sw.swizzle(from, to);
		assertEquals(expected, to);
	}

	@Test
	public void test_inversion() throws Exception {
		Swizzle sw;
		VectorElement from;
		VectorElement to;
		VectorElement expected;

		sw = SwizzleFactory.compile("x", "x");
		from = new VectorElement(new ElementInt(1));
		to = VectorElement.createInt(1, 0);
		expected = from.copy();
		sw.swizzle(from, to);
		sw.invert().swizzle(to, from);
		assertEquals(expected, from);

		sw = SwizzleFactory.compile("xyz", "zxy");
		from = new VectorElement(new ElementInt(1), new ElementInt(2),
				new ElementInt(3));
		to = VectorElement.createInt(3, 0);
		expected = from.copy();
		sw.swizzle(from, to);
		sw.invert().swizzle(to, from);
		assertEquals(expected, from);

		sw = SwizzleFactory.compile("abcde", "ebdca");
		from = new VectorElement(new ElementInt(1), new ElementInt(2),
				new ElementInt(3), new ElementInt(4), new ElementInt(5));
		to = VectorElement.createInt(5, 0);
		expected = from.copy();
		sw.swizzle(from, to);
		sw.invert().swizzle(to, from);
		assertEquals(expected, from);
	}

	static final int ITERATIONS = 10000000;

	// Microbenchmark for specialised swizzle classes. If you're curious, try
	// commenting out all but SwizzleN from
	// org.vpac.ndg.query.math.SwizzleFactory.collate
	@Test
	public void test_speed() throws Exception {
		for (int i = 1; i <= 4; i++) {
			VectorReal sum = _test_speed_n(i);
			System.out.println(sum);
		}
	}

	private VectorReal _test_speed_n(int n) {
		VectorReal from = VectorReal.createEmpty(n);
		VectorReal to = VectorReal.createEmpty(4);
		VectorReal sum = VectorReal.createEmpty(4);
		Swizzle sw = SwizzleFactory.resize(n, to.size());
		for (int i = 0; i < ITERATIONS; i++) {
			for (int j = 0; j < n; j++) {
				from.set(j, Math.random());
			}
			sw.swizzle(from, to);
			// Make sure this doesn't get optimised out by keeping a sum.
			sum.add(to);
		}
		return sum;
	}

}
