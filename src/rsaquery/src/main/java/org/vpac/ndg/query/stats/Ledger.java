package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.stats.BucketingStrategyFactory;

/**
 * Counts occurrences of combinations of numbers.
 * @author Alex Fraser
 */
public class Ledger implements Foldable<Ledger>, Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Map<List<Double>, Long> entries;
	private List<Double> bucketedCombination;
	private List<Double> currentCombination;
	private Long currentCount;
	private List<BucketingStrategy> bss;

	public Ledger() {
		entries = new HashMap<>();
		currentCombination = new ArrayList<>();
		bucketedCombination = new ArrayList<>();
		currentCount = 0L;
		bss = new ArrayList<>();
	}

	public void add(List<Double> combination) {
		bucketedCombination.clear();
		for (int i = 0; i < combination.size(); i++) {
			Double component = combination.get(i);
			Double bucketedValue;
			if (component != null)
				bucketedValue = bss.get(i).computeBucketBounds(component)[0];
			else
				bucketedValue = null;
			bucketedCombination.add(bucketedValue);
		}
		if (!bucketedCombination.equals(currentCombination)) {
			currentCount = entries.get(bucketedCombination);
			if (currentCount == null)
				currentCount = 0L;
			currentCombination = new ArrayList<>(bucketedCombination);
		}
		currentCount++;
		entries.put(currentCombination, currentCount);
	}

	public long get(List<Double> combination) {
		Long count = entries.get(combination);
		if (count == null)
			return 0;
		else
			return count;
	}

	public Double maxValue() {
		Collection<Long> values = entries.values();
		Long maxVal = Long.MIN_VALUE;

		for (Long currentVal : values) {
			if (currentVal != null && currentVal > maxVal) {
				maxVal = currentVal;
			}
		}
		return maxVal.doubleValue();
	}

	public Double minValue() {
		Collection<Long> values = entries.values();
		Long minVal = Long.MAX_VALUE;

		for (Long currentVal : values) {
			if (currentVal != null && currentVal < minVal) {
				minVal = currentVal;
			}
		}
		return minVal.doubleValue();
	}

	public Double maxKey(int column) {
		Set<List<Double>> keys = entries.keySet();
		Double maxKey = Double.MIN_VALUE;
		Double currentKey;

		for (List<Double> key : keys) {
			currentKey = key.get(column);
			if (currentKey != null && currentKey > maxKey) {
				maxKey = currentKey;
			}
		}
		return maxKey;
	}

	public Double minKey(int column) {
		Set<List<Double>> keys = entries.keySet();
		Double minKey = Double.MAX_VALUE;
		Double currentKey;

		for (List<Double> key : keys) {
			currentKey = key.get(column);
			if (currentKey != null && currentKey < minKey) {
				minKey = currentKey;
			}
		}
		return minKey;
	}

	public int size() {
		return entries.size();
	}

	public Set<List<Double>> keySet() {
		return entries.keySet();
	}

	public Set<Entry<List<Double>, Long>> entrySet() {
		return entries.entrySet();
	}

	public Collection<Long> values() {
		return entries.values();
	}

	public Ledger copy() {
		Ledger res = new Ledger();
		res.entries.putAll(entries);
		res.bss = new ArrayList<>(bss);
		return res;
	}

	@Override
	public Ledger fold(Ledger other) {
		if (bss.size() == 0 && entries.size() == 0) {
			bss.addAll(other.bss);
		} else if (!bss.equals(other.bss)) {
			// With additional metadata, it might be possible to splice
			// heterogeneous Ledgers. But for now...
			throw new QueryException(String.format(
				"Ledgers are incompatible: bucketing strategies differ: %s vs %s",
				bss, other.bss));
		}
		Ledger res = copy();
		for (List<Double> key : other.entries.keySet()) {
			Long count = entries.get(key);
			if (count == null)
				count = 0L;
			count += other.entries.get(key);
			res.entries.put(key, count);
		}
		return res;
	}

	/**
	 * Remove all but the specified columns from this ledger. Duplicate rows
	 * will be folded together by summing.
	 * @param columns The column indices to keep.
	 * @return a new Ledger.
	 */
	public Ledger filterColumns(List<Integer> columns) {
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		Ledger res = new Ledger();

		for (Integer i : columns) {
			res.bss.add(bf.create(this.bss.get(i).getDef()));
		}

		for (Entry<List<Double>, Long> entry : entries.entrySet()) {
			List<Double> key = new ArrayList<>(columns.size());
			for (Integer i : columns) {
				key.add(entry.getKey().get(i));
			}
			Long count = res.entries.get(key);
			if (count == null)
				count = 0L;
			count += entry.getValue();
			res.entries.put(key, count);
		}

		return res;
	}

	/**
	 * Remove all but the specified rows from this ledger.
	 * @return a new Ledger.
	 */
	public Ledger filterRows(int column, Set<Double> ids) {
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		Ledger res = new Ledger();

		for (BucketingStrategy bs : bss) {
			res.bss.add(bf.create(bs.getDef()));
		}

		for (Entry<List<Double>, Long> entry : entries.entrySet()) {
			if (!ids.contains(entry.getKey().get(column)))
				continue;
			List<Double> key = new ArrayList<>(entry.getKey().size());
			key.addAll(entry.getKey());
			res.entries.put(key, entry.getValue());
		}

		return res;
	}

	private boolean intersects(double[] bounds, double lower, double upper) {
		if (bounds[0] == lower)
			return true;
		else if (bounds[0] >= upper)
			return false;
		else if (bounds[1] <= lower)
			return false;
		return true;
	}

	private boolean intersects(double[] bounds, List<Double> lower, List<Double> upper) {
		for (int i = 0; i < lower.size(); i++) {
			double lb = lower.get(i);
			double ub = upper.get(i);
			if (intersects(bounds, lb, ub))
				return true;
		}
		return false;
	}

	/**
	 * Remove all but the specified rows from this ledger.
	 * @return a new Ledger.
	 */
	public Ledger filterRows(int column, List<Double> lower, List<Double> upper) {
		if (lower == null && upper != null)
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		if (lower == null)
			return copy();

		if (lower.size() != upper.size())
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		Ledger res = new Ledger();

		for (BucketingStrategy bs : bss) {
			res.bss.add(bf.create(bs.getDef()));
		}

		BucketingStrategy bs = res.bss.get(column);
		for (Entry<List<Double>, Long> entry : entries.entrySet()) {
			Double component = entry.getKey().get(column);
			if (component == null) {
				// No way to request null rows.
				continue;
			}
			double[] bounds = bs.computeBucketBounds(component);
			if (!intersects(bounds, lower, upper))
				continue;
			List<Double> key = new ArrayList<>(entry.getKey().size());
			key.addAll(entry.getKey());
			res.entries.put(key, entry.getValue());
		}

		return res;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int width = 0;
		for (List<Double> key : entries.keySet()) {
			width = key.size();
			break;
		}
		sb.append(String.format("Ledger(%dx%d)", width, entries.size()));
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setBucketingStrategies(List<String> sbss) {
		BucketingStrategyFactory bf = new BucketingStrategyFactory();
		bss = sbss.stream()
			.map(sbs -> bf.create(sbs))
			.collect(Collectors.toList());
	}

	public List<String> getBucketingStrategies() {
		return bss.stream()
			.map(bs -> bs.getDef())
			.collect(Collectors.toList());
	}

	public void _setBucketingStrategies(List<BucketingStrategy> bss) {
		this.bss = bss;
	}

	public List<BucketingStrategy> _getBucketingStrategies() {
		return bss;
	}

	public Map<List<Double>, Long> getEntries() {
		return entries;
	}

	public void setEntries(Map<List<Double>, Long> entries) {
		this.entries = entries;
	}

	/**
	 * @return the number of coordinates that were sampled to produce
	 * this ledger. This is usually the number of output pixels, i.e.
	 * independent of the number of input channels.
	 */
	public long totalCount() {
		long volume = 0L;
		for (Long count : entries.values()) {
			volume += count;
		}
		return volume;
	}

}
