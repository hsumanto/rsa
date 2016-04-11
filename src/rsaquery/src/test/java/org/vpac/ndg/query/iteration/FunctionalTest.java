package org.vpac.ndg.query.iteration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class FunctionalTest {

	@Test
	public void test_flatten() throws Exception {
		List<List<Integer>> xss = new ArrayList<List<Integer>>();
		xss.add(Arrays.asList(new Integer[] {1, 2, 3}));
		xss.add(Arrays.asList(new Integer[] {4, 5, 6}));
		xss.add(Arrays.asList(new Integer[] {7, 8, 9}));

		Integer[] flat = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9};

		int i = 0;
		for (Integer x : new Flatten<Integer>(xss)) {
			assertEquals(flat[i], x);
			i++;
		}
		assertEquals(flat.length, i);
	}

	@Test
	public void test_zip() throws Exception {
		List<List<Integer>> xss = new ArrayList<List<Integer>>();
		xss.add(Arrays.asList(new Integer[] {1, 2, 3}));
		xss.add(Arrays.asList(new Integer[] {4, 5, 6}));
		xss.add(Arrays.asList(new Integer[] {7, 8, 9}));

		Integer[][] zip = new Integer[][] {
				new Integer[] {1, 4, 7},
				new Integer[] {2, 5, 8},
				new Integer[] {3, 6, 9}};

		int i = 0;
		for (Iterable<Integer> xs : new ZipN<Integer>(xss)) {
			int j = 0;
			for (Integer x : xs) {
				assertEquals(zip[i][j], x);
				j++;
			}
			i++;
		}
		assertEquals(zip.length, i);
	}

}
