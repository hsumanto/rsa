package org.vpac.ndg.query.stats;

import java.util.ArrayList;
import java.util.List;

public class MockData {

	/**
	 * @param n The number of sides on the first die.
	 * @param m The number of sides on the second die.
	 * @return A list of permutations for the rolling of two dice. This should
	 *         result in a perfect normal distribution.
	 */
	public static List<Integer> permute(int n, int m) {
		List<Integer> permutations = new ArrayList<Integer>();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				permutations.add(i + j);
			}
		}
		return permutations;
	}
}
