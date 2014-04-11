package org.vpac.ndg.query.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

	public Cats(ScalarElement prototype) {
		this.prototype = prototype;
		currentCategory = null;
		currentHist = null;
		categories = new HashMap<ScalarElement, Hist>();
	}

	public void update(ScalarElement category, ScalarElement value) {
		if (!value.isValid())
			return;
		getHistogram(category).update(value);
	}

	Hist getHistogram(ScalarElement category) {
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

}
