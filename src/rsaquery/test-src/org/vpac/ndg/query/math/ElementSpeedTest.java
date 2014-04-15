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

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
@RunWith(BlockJUnit4ClassRunner.class)
public class ElementSpeedTest extends TestCase {

	static final int ITERATIONS = 250000000;

	@Rule
	public MethodRule benchmarkRun = new BenchmarkRule();

	/**
	 * This test should run roughly as fast as {@link #test_vectorcomponents()}.
	 */
	@Test
	public void test_scalarcomponents() {
		ScalarElement e = new ElementLong();
		for (int i = 0; i < ITERATIONS; i++) {
			ScalarElement[] components = e.getComponents();
			for (int j = 0; j < components.length; j++) {
				components[j].add(1);
			}
		}
	}

	@Test
	public void test_vectorcomponents() {
		VectorElement e = new VectorElement(new ElementLong());
		for (int i = 0; i < ITERATIONS; i++) {
			ScalarElement[] components = e.getComponents();
			for (int j = 0; j < components.length; j++) {
				components[j].add(1);
			}
		}
	}

}
