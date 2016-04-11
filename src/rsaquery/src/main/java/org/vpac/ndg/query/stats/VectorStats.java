package org.vpac.ndg.query.stats;

import java.io.Serializable;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * Basic statistics (min, max, mean, standard deviation). The components
 * of vector elements are grouped separately.
 * @author Alex Fraser
 */
public class VectorStats implements Foldable<VectorStats>, Serializable {

	private static final long serialVersionUID = 1L;

	private Stats[] components;

	public VectorStats(int nComponents) {
		components = new Stats[nComponents];
		for (int i = 0; i < components.length; i++) {
			components[i] = new Stats();
		}
	}

	public void update(Element<?> value) {
		ScalarElement[] es = value.getComponents();
		for (int i = 0; i < components.length; i++)
			components[i].update(es[i]);
	}

	@Override
	public VectorStats fold(VectorStats other) {
		VectorStats res = new VectorStats(components.length);

		for (int i = 0; i < components.length; i++) {
			res.components[i] = components[i].fold(other.components[i]);
		}

		return res;
	}

	public long[] getCount() {
		long[] es = new long[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getCount();
		}
		return es;
	}

	public double[] getMin() {
		double[] es = new double[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMin();
		}
		return es;
	}

	public double[] getMax() {
		double[] es = new double[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMax();
		}
		return es;
	}

	public double[] getMean() {
		double[] es = new double[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getMean();
		}
		return es;
	}

	public double[] getStdDev() {
		double[] es = new double[components.length];
		for (int i = 0; i < es.length; i++) {
			es[i] = components[i].getStdDev();
		}
		return es;
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