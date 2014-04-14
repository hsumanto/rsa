package org.vpac.ndg.query.stats;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Groups values together based on their intrinsic distribution. The components
 * of vector elements are grouped separately.
 *
 * @author Alex Fraser
 */
public class VectorCats implements Foldable<VectorCats> {

	private Element<?> prototype;
	private Cats[] components;

	public VectorCats(Element<?> prototype) {
		this.prototype = prototype;
		ScalarElement[] es = prototype.getComponents();
		components = new Cats[es.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = new Cats(es[i]);
		}
	}

	public void update(ScalarElement category, Element<?> value) {
		ScalarElement[] es = value.getComponents();
		for (int i = 0; i < components.length; i++)
			components[i].update(category, es[i]);
	}

	@Override
	public VectorCats fold(VectorCats other) {
		VectorCats res = new VectorCats(prototype);

		for (int i = 0; i < components.length; i++) {
			res.components[i] = components[i].fold(other.components[i]);
		}

		return res;
	}

	public Cats[] getComponents() {
		return components;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[\n");

		boolean firstComponent = true;
		for (int i = 0; i < components.length; i++) {
			Cats c = components[i];

			if (!firstComponent)
				sb.append(",\n");
			else
				firstComponent = false;

			sb.append(c.toString());
		}
		sb.append("\n]");

		return sb.toString();
	}
}
