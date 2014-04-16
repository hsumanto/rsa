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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class ElementTest {

	@Test
	public void test_ternaryCoercion() throws Exception {
		ElementByte opA = new ElementByte((byte) 1);
		ElementByte opB = new ElementByte((byte) 2);
		ElementFloat result = new ElementFloat();
		result.divOf(opA, opB);
		assertEquals(0.5, result.floatValue(), 0.01);
	}

	@Test
	public void test_smallestAndLargest() {
		ScalarElement e;

		e = new ElementByte();
		e.minimise();
		assertEquals(-128, e.byteValue());
		e.maximise();
		assertEquals(127, e.byteValue());

		e = new ElementShort();
		e.minimise();
		assertEquals(-32768, e.shortValue());
		e.maximise();
		assertEquals(32767, e.shortValue());

		e = new ElementInt();
		e.minimise();
		assertEquals(-2147483648, e.intValue());
		e.maximise();
		assertEquals(2147483647, e.intValue());

		e = new ElementLong();
		e.minimise();
		assertEquals(-9223372036854775808L, e.longValue());
		e.maximise();
		assertEquals(9223372036854775807L, e.longValue());

		e = new ElementFloat();
		e.minimise();
		assertEquals(Float.NEGATIVE_INFINITY, e.floatValue(), 0.00001);
		assertEquals(Double.NEGATIVE_INFINITY, e.doubleValue(), 0.00001);
		assertEquals(-1, e.compareTo(Long.MIN_VALUE));
		e.maximise();
		assertEquals(Float.POSITIVE_INFINITY, e.floatValue(), 0.00001);
		assertEquals(Double.POSITIVE_INFINITY, e.doubleValue(), 0.00001);
		assertEquals(1, e.compareTo(Long.MAX_VALUE));

		e = new ElementDouble();
		e.minimise();
		assertEquals(Float.NEGATIVE_INFINITY, e.floatValue(), 0.00001);
		assertEquals(Double.NEGATIVE_INFINITY, e.doubleValue(), 0.00001);
		assertEquals(-1, e.compareTo(Long.MIN_VALUE));
		e.maximise();
		assertEquals(Float.POSITIVE_INFINITY, e.floatValue(), 0.00001);
		assertEquals(Double.POSITIVE_INFINITY, e.doubleValue(), 0.00001);
		assertEquals(1, e.compareTo(Long.MAX_VALUE));
	}
}
