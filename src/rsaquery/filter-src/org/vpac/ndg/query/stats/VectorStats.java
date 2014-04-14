package org.vpac.ndg.query.stats;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorElement;

/**
 * Basic statistics (min, max, mean, standard deviation). The components
 * of vector elements are grouped separately.
 * @author Alex Fraser
 */
public class VectorStats implements Foldable<VectorStats> {

	private Element<?> prototype;
	private Stats[] components;

	public VectorStats(Element<?> prototype) {
		this.prototype = prototype;
		ScalarElement[] es = prototype.getComponents();
		components = new Stats[es.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = new Stats(es[i]);
		}
	}

	public void update(Element<?> value) {
		ScalarElement[] es = value.getComponents();
		for (int i = 0; i < components.length; i++)
			components[i].update(es[i]);
	}

	@Override
	public VectorStats fold(VectorStats other) {
		VectorStats res = new VectorStats(prototype);

		for (int i = 0; i < components.length; i++) {
			res.components[i] = components[i].fold(other.components[i]);
		}

		return res;
	}

	public VectorElement getCount() {
		ScalarElement[] es = new ScalarElement[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getCount();
		}
		return new VectorElement(es);
	}

	public VectorElement getMin() {
		ScalarElement[] es = new ScalarElement[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMin();
		}
		return new VectorElement(es);
	}

	public VectorElement getMax() {
		ScalarElement[] es = new ScalarElement[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMax();
		}
		return new VectorElement(es);
	}

	public VectorElement getMean() {
		ScalarElement[] es = new ScalarElement[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMean();
		}
		return new VectorElement(es);
	}

	public VectorElement getStdDev() {
		ScalarElement[] es = new ScalarElement[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getStdDev();
		}
		return new VectorElement(es);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[\n");

		boolean firstComponent = true;
		for (int i = 0; i < components.length; i++) {
			Stats c = components[i];

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