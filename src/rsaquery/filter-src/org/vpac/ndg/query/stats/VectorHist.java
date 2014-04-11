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
public class VectorHist implements Foldable<VectorHist> {

	private Element<?> prototype;
	private Hist[] components;

	public VectorHist(Element<?> prototype) {
		this.prototype = prototype;
		ScalarElement[] es = prototype.getComponents();
		components = new Hist[es.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = new Hist(es[i]);
		}
	}

	public void update(Element<?> value) {
		ScalarElement[] es = prototype.getComponents();
		for (int i = 0; i < components.length; i++)
			components[i].update(es[i]);
	}

	@Override
	public VectorHist fold(VectorHist other) {
		VectorHist res = new VectorHist(prototype);

		for (int i = 0; i < components.length; i++) {
			res.components[i] = components[i].fold(other.components[i]);
		}

		return res;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");

		boolean firstComponent = true;
		for (int i = 0; i < components.length; i++) {
			Hist c = components[i];

			if (!firstComponent)
				sb.append(",\n");
			else
				firstComponent = false;

			sb.append(c.toString());
		}
		sb.append("\n}");

		return sb.toString();
	}

}
