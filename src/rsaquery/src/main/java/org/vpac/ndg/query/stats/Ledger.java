package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Counts occurrences of combinations of numbers.
 * @author Alex Fraser
 */
public class Ledger implements Foldable<Ledger>, Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Map<List<Double>, Long> combinations;
	private List<Double> bucketedCombination;
	private List<Double> currentCombination;
	private Long currentCount;
	private List<BucketingStrategy> bss;

	public Ledger() {
		combinations = new HashMap<>();
		currentCombination = new ArrayList<>();
		bucketedCombination = new ArrayList<>();
		currentCount = 0L;
		bss = new ArrayList<>();
	}

	public void add(List<Double> combination) {
		bucketedCombination.clear();
		for (int i = 0; i < combination.size(); i++) {
			double component = combination.get(i);
			double bucketedValue = bss.get(i).computeBucketBounds(component)[0];
			bucketedCombination.add(bucketedValue);
		}
		if (!bucketedCombination.equals(currentCombination)) {
			currentCount = combinations.get(bucketedCombination);
			if (currentCount == null)
				currentCount = 0L;
			currentCombination = new ArrayList<>(bucketedCombination);
		}
		currentCount++;
		combinations.put(currentCombination, currentCount);
	}

	public long get(List<Double> combination) {
		Long count = combinations.get(combination);
		if (count == null)
			return 0;
		else
			return count;
	}

	public Set<List<Double>> keySet() {
		return combinations.keySet();
	}

	public Ledger copy() {
		Ledger res = new Ledger();
		res.combinations.putAll(combinations);
		res.bss = new ArrayList<>(bss);
		return res;
	}

	@Override
	public Ledger fold(Ledger other) {
		if (bss.size() == 0 && combinations.size() == 0) {
			bss.addAll(other.bss);
		} else if (!bss.equals(other.bss)) {
			// With additional metadata, it might be possible to splice
			// heterogeneous Ledgers. But for now...
			throw new QueryException(String.format(
				"Ledgers are incompatible: bucketing strategies differ: %s vs %s",
				bss, other.bss));
		}
		Ledger res = copy();
		for (List<Double> key : other.combinations.keySet()) {
			Long count = combinations.get(key);
			if (count == null)
				count = 0L;
			count += other.combinations.get(key);
			res.combinations.put(key, count);
		}
		return res;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int width = 0;
		for (List<Double> key : combinations.keySet()) {
			width = key.size();
			break;
		}
		sb.append(String.format("Ledger(%dx%d)", width, combinations.size()));
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setBucketingStrategies(List<BucketingStrategy> bss) {
		this.bss = bss;
	}

	public List<BucketingStrategy> getBucketingStrategies() {
		return bss;
	}

	public Map<List<Double>, Long> getCombinations() {
		return combinations;
	}

	public void setCombinations(Map<List<Double>, Long> combinations) {
		this.combinations = combinations;
	}

}
