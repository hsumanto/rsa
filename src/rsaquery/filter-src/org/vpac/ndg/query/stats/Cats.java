package org.vpac.ndg.query.stats;

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
public class Cats implements Foldable<Cats> {

	private Map<ScalarElement, Hist> categories;
	private ScalarElement currentCategory;
	private Hist currentHist;
	private ScalarElement prototype;

	/**
	 * @param prototype The data type of the values to store (not the
	 *            categories).
	 */
	public Cats(ScalarElement prototype) {
		this.prototype = prototype;
		currentCategory = null;
		currentHist = null;
		categories = new HashMap<ScalarElement, Hist>();
	}

	public void update(ScalarElement category, ScalarElement value) {
		if (!value.isValid())
			return;
		getOrCreateHistogram(category).update(value);
	}

	private Hist getOrCreateHistogram(ScalarElement category) {
		if (category.equals(currentCategory))
			return currentHist;

		currentHist = categories.get(category);
		if (currentHist == null) {
			currentHist = new Hist(prototype);
			categories.put(category.copy(), currentHist);
		}
		return currentHist;
	}

	@Override
	public Cats fold(Cats other) {
		Set<ScalarElement> keys = new HashSet<ScalarElement>();
		keys.addAll(categories.keySet());
		keys.addAll(other.categories.keySet());

		Cats res = new Cats(prototype);
		for (ScalarElement key : keys) {
			Hist histA = categories.get(key);
			Hist histB = other.categories.get(key);
			res.categories.put(key, histA.fold(histB));
		}

		return res;
	}

	public Set<ScalarElement> getKeys() {
		return categories.keySet();
	}

	public Hist get(ScalarElement key) {
		return categories.get(key);
	}

	public Set<Entry<ScalarElement, Hist>> getEntries() {
		return categories.entrySet();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cats(\n");
		boolean first = true;
		for (Entry<ScalarElement, Hist> e : categories.entrySet()) {
			if (!first)
				sb.append(",\n");
			else
				first = false;
			sb.append(String.format("%s: %s", e.getKey(), e.getValue()));
		}
		sb.append(")");
		return sb.toString();
	}
}
