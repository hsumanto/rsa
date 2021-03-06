/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.ndg.query.math;

import java.io.Serializable;

import org.vpac.ndg.query.coordinates.HasRank;


/**
 * A simple axis-aligned box.
 * @author Alex Fraser
 *
 */
public class BoxReal implements HasRank, Serializable {

	private static final long serialVersionUID = 1L;
	protected VectorReal min;
	protected VectorReal max;

	protected BoxReal() {
	}

	/**
	 * Creates a new box of zero volume. All components are set to zero.
	 * @param dimensions The number of dimensions.
	 */
	public BoxReal(int dimensions) {
		this.min = new VectorReal(dimensions);
		this.max = new VectorReal(dimensions);
	}

	/**
	 * Creates a new box of zero volume, centered on a point.
	 * @param point The point to centre the box on.
	 */
	public BoxReal(VectorReal point) {
		this(point.size());
		this.min.set(point);
		this.max.set(point);
	}

	public BoxReal(VectorReal min, VectorReal max) {
		this(min.size());
		this.min.set(min);
		this.max.set(max);
	}

	/**
	 * @param bounds The extents specified as a string, separated by spaces:
	 *        "xmin ymin xmax ymax" or "xmin ymin zmin xmax ymax zmax"
	 */
	public BoxReal(String bounds) {
		String[] components = bounds.split(" ");
		int dimensions = components.length / 2;
		min = VectorReal.createEmpty(dimensions);
		max = VectorReal.createEmpty(dimensions);
		int i;

		for (i = 0; i < dimensions; i++) {
			// Target axes are in reverse order
			int axis = dimensions - 1 - i;
			min.set(axis, Double.parseDouble(components[i]));
			max.set(axis, Double.parseDouble(components[dimensions + i]));
		}
	}

	/**
	 * Grow this box so that it includes <em>other</em>.
	 * @param other The box to expand to.
	 */
	public void union(BoxReal other) {
		this.min.min(other.getMin());
		this.max.max(other.getMax());
	}

	/**
	 * Grow this box so that it includes <em>other</em>, but ignore axes on
	 * which the other box has zero size.
	 * @param other The box to expand to.
	 */
	public void unionIfPositive(BoxReal other) {
		VectorReal size = getSize();
		VectorReal otherSize = other.getSize();
		for (int i = 0; i < size.size(); i++) {
			if (otherSize.get(i) <= 0)
				continue;
			double componentMin;
			double componentMax;
			if (size.get(i) <= 0) {
				componentMin = other.min.get(i);
				componentMax = other.max.get(i);
			} else {
				componentMin = Math.min(min.get(i), other.min.get(i));
				componentMax = Math.max(max.get(i), other.max.get(i));
			}
			min.set(i, componentMin);
			max.set(i, componentMax);
		}
	}

	/**
	 * Grow this box so that it includes the point <em>other</em>.
	 * @param other The point to expand to.
	 */
	public void union(VectorReal other) {
		this.min.min(other);
		this.max.max(other);
	}

	/**
	 * Shrink this box so it does not extend beyond <em>other</em>.
	 * @param other The box to be constrained by.
	 */
	public void intersect(BoxReal other) {
		this.min.max(other.getMin());
		this.max.min(other.getMax());
		// Make sure the new bounds aren't inside out (max < min). If they are,
		// shift min just enough to make a zero-sized box.
		this.min.min(this.max);
	}

	public VectorReal getMin() {
		return min;
	}

	public void setMin(VectorReal min) {
		this.min.set(min);
	}

	public VectorReal getMax() {
		return max;
	}

	public void setMax(VectorReal max) {
		this.max.set(max);
	}

	public VectorReal getSize() {
		return getMax().subNew(getMin());
	}

	@Override
	public String toString() {
		return String.format("Box(%s - %s)", min, max);
	}

	public BoxReal copy() {
		BoxReal res = new BoxReal();
		res.min = min.copy();
		res.max = max.copy();
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoxReal other = (BoxReal) obj;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (min == null) {
			if (other.min != null)
				return false;
		} else if (!min.equals(other.min))
			return false;
		return true;
	}

	@Override
	public int getRank() {
		return min.size();
	}
}
