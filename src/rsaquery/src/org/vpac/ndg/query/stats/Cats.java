package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Groups values into arbitrary buckets (categories to be provided by user). 
 * @author Alex Fraser
 */
public class Cats implements Foldable<Cats>, Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Map<Integer, Hist> categories;
	private ScalarElement currentCategory;
	private Hist currentHist;

	/**
	 * @param prototype The data type of the values to store (not the
	 *            categories).
	 */
	public Cats() {
		currentCategory = null;
		currentHist = null;
		categories = new HashMap<Integer, Hist>();
	}

	public Cats copy() {
		Cats res = new Cats();
		for (Entry<Integer, Hist> entry : categories.entrySet()) {
			res.categories.put(entry.getKey(), entry.getValue().copy());
		}
		return res;
	}

	public void update(ScalarElement category, ScalarElement value) {
		if (!value.isValid() || !category.isValid())
			return;
		getOrCreateHistogram(category.intValue()).update(value);
	}

	private Hist getOrCreateHistogram(Integer category) {
		if (category.equals(currentCategory))
			return currentHist;

		currentHist = categories.get(category);
		if (currentHist == null) {
			currentHist = new Hist();
			categories.put(category, currentHist);
		}
		return currentHist;
	}

	@Override
	public Cats fold(Cats other) {
		Set<Integer> keys = new HashSet<Integer>();
		keys.addAll(categories.keySet());
		keys.addAll(other.categories.keySet());

		Cats res = new Cats();
		for (Integer key : keys) {
			Hist histA = categories.get(key);
			if (histA == null)
				histA = new Hist();
			Hist histB = other.categories.get(key);
			if (histB == null)
				histB = new Hist();
			res.categories.put(key, histA.fold(histB));
		}

		return res;
	}

	public Cats optimise() {
		Cats res = new Cats();
		for (Entry<Integer, Hist> entry : categories.entrySet()) {
			Hist hist = entry.getValue().optimise();
			if (hist.summarise().getCount() > 0)
				res.categories.put(entry.getKey(), hist);
		}
		return res;
	}

	public Hist summarise() {
		Hist summary = new Hist();
		summary.getBuckets().clear();
		for (Hist hist : categories.values()) {
			summary = summary.fold(hist);
		}
		return summary;
	}

	/**
	 * Create a new set of categories, filtered by a list of keys.
	 *
	 * @param filterCats The keys to filter by. If null, all categories will
	 *            match.
	 * @return A new set of categories.
	 */
	public Cats filterByCategory(List<Integer> filterCats) {
		if (filterCats == null)
			return copy();

		Cats res = new Cats();
		for (Integer key : categories.keySet()) {
			if (filterCats.contains(key))
				res.categories.put(key, categories.get(key).copy());
		}
		return res;
	}

	/**
	 * Create a new set of categories whose histograms contain only buckets that
	 * match some criteria.
	 *
	 * @param lower The lower bounds of the buckets. Parallel list with upper.
	 *            If null, all buckets will match.
	 * @param upper The upper bounds of the buckets. Parallel list with lower.
	 *            If null, all buckets will match.
	 * @return A new set of categories containing only buckets that match one of
	 *         the lower-upper bound pairs.
	 */
	public Cats filterByRange(List<Double> lower, List<Double> upper) {
		if (lower == null && upper != null)
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		if (lower == null)
			return copy();

		if (lower.size() != upper.size())
			throw new IndexOutOfBoundsException("Lower and upper bounds don't match");

		Cats res = new Cats();
		for (Entry<Integer, Hist> entry : categories.entrySet()) {
			Hist hist = entry.getValue().filterByRange(lower, upper);
			res.categories.put(entry.getKey(), hist);
		}
		return res;
	}

	public Set<Integer> getKeys() {
		return categories.keySet();
	}

	public Hist get(Integer key) {
		return categories.get(key);
	}

	public Set<Entry<Integer, Hist>> getEntries() {
		return categories.entrySet();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cats(\n");
		boolean first = true;
		for (Entry<Integer, Hist> e : categories.entrySet()) {
			if (!first)
				sb.append(",\n");
			else
				first = false;
			sb.append(String.format("%s: %s", e.getKey(), e.getValue()));
		}
		sb.append(")");
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<Integer, Hist> getCategories() {
		return categories;
	}

	public void setCategories(Map<Integer, Hist> categories) {
		this.categories = categories;
	}
}
