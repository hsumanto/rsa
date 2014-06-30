package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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
			if (hist.getSummary().getCount() > 0)
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
