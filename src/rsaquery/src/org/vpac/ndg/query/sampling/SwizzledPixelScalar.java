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

package org.vpac.ndg.query.sampling;

import java.io.IOException;

import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.Swizzle;
import org.vpac.ndg.query.math.VectorReal;

/**
 * A scalar pixel source that reorders the dimensions of its parent.
 * @author Alex Fraser
 */
public class SwizzledPixelScalar implements PixelSourceScalar {

	PixelSourceScalar source;
	BoxReal bounds;
	Swizzle swizzle;
	VectorReal swizzledCo;
	Prototype prototype;

	public SwizzledPixelScalar(PixelSourceScalar source, Swizzle swizzle,
			int sourceRank, int rank) {

		swizzledCo = VectorReal.createEmpty(sourceRank);
		this.source = source;
		this.swizzle = swizzle;

		prototype = source.getPrototype().copy();
		// Swizzle dimensions. When promoting (rank > sourceRank), this may
		// result in some null dimensions (see Swizzle.SwizzleOp0).
		String[] dims = new String[rank];
		swizzle.invert().swizzle(source.getPrototype().getDimensions(),
				prototype.getDimensions());
		prototype.setDimensions(dims);
		bounds = new BoxReal(rank);
		swizzle.invert().swizzle(source.getBounds(), bounds);
	}

	@Override
	public ScalarElement getScalarPixel(VectorReal co) throws IOException {
		swizzle.swizzle(co, swizzledCo);
		return source.getScalarPixel(swizzledCo);
	}

	@Override
	public Element<?> getPixel(VectorReal co) throws IOException {
		return getScalarPixel(co);
	}

	@Override
	public BoxReal getBounds() {
		return bounds;
	}

	@Override
	public int getRank() {
		return bounds.getRank();
	}

	@Override
	public Prototype getPrototype() {
		return source.getPrototype();
	}

	@Override
	public String[] getDimensions() {
		return prototype.getDimensions();
	}

	@Override
	public String toString() {
		return String.format("SwizzledPixel(%s)", source);
	}
}
