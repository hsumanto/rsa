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

// THIS IS GENERATED CODE. Do not modify this file. See VectorX_gen.py.

package org.vpac.ndg.query.math;

import java.io.Serializable;
import java.util.Arrays;

import ucar.ma2.Index;

/**
 * A short array of numbers that can be used in arithmetic.
 * 
 * @author Alex Fraser
 */
public class VectorInt implements Serializable {

	private static final long serialVersionUID = 1L;
	long[] components;

	protected VectorInt() {
	}
	protected VectorInt(int size) {
		components = new long[size];
	}

	public static VectorInt createEmpty(int capacity) {
		return new VectorInt(capacity);
	}

	public static VectorInt createEmpty(int capacity, long fill) {
		VectorInt res = new VectorInt(capacity);
		for (int i = 0; i < res.components.length; i++) {
			res.components[i] = fill;
		}
		return res;
	}

	public static VectorInt create(long... components) {
		if (components.length == 0 || components.length > 4) {
			throw new IllegalArgumentException(
					"Vectors must have from 1-4 components.");
		}
		VectorInt res = new VectorInt();
		res.components = components.clone();
		return res;
	}

	public static VectorInt fromInt(int... components) {
		if (components.length == 0 || components.length > 4) {
			throw new IllegalArgumentException(
					"Vectors must have from 1-4 components.");
		}
		VectorInt res = createEmpty(components.length);
		for (int i = 0; i < components.length; i++)
			res.components[i] = components[i];
		return res;
	}

	public VectorInt copy() {
		VectorInt res = new VectorInt();
		res.components = this.components.clone();
		return res;
	}

	/**
	 * @return The index of an axis, or -1 if the axis is not present.
	 */
	public int indexOf(String axis) {
		return ComponentLUT.indexOf(components.length, axis);
	}

	// CASTING

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		boolean first = true;
		for (long c : components) {
			if (first)
				first = false;
			else
				buf.append(", ");
			buf.append(c);
		}
		buf.append(")");
		return buf.toString();
	}

	public void set(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = other;
	}
	public void set(VectorReal other) {
		for (int i = 0; i < components.length; i++)
			components[i] = (long)other.components[i];
	}
	public void set(VectorInt other) {
		for (int i = 0; i < components.length; i++)
			components[i] = other.components[i];
	}

	public long[] asArray() {
		return components;
	}

	public long volume() {
		long vol = 1;
		for (int i = 0; i < components.length; i++) {
			vol *= components[i];
		}
		return vol;
	}

	public int size() {
		return components.length;
	}
	public long get(int i) {
		return components[i];
	}
	public void set(int i, long other) {
		components[i] = other;
	}

	/**
	 * Finds the index pointed to by this vector in an image.
	 * 
	 * @param shape The shape of the image.
	 * @return The index of the pixel (assuming the image is stored as a 1D
	 *         array).
	 */
	public long toPixelIndex(VectorInt shape) {
		// Iterate in reverse: x, y, z, w. Later dimensions depend on the result
		// of previous ones.
		long idx = 0;
		long vol = 1;
		for (int i = components.length - 1; i >= 0; i--) {
			idx += components[i] * vol;
			vol *= shape.components[i];
		}
		return idx;
	}

	/**
	 * @return The X component (last; synonym for a).
	 */
	public long getX() {
		return components[ComponentLUT.getX(components.length)];
	}
	/**
	 * Set the X component (last; synonym for a).
	 * @param value The value to assign to the component.
	 */
	public void setX(long value) {
		components[ComponentLUT.getX(components.length)] = value;
	}

	/**
	 * @return The Y component (second last; synonym for b).
	 */
	public long getY() {
		return components[ComponentLUT.getY(components.length)];
	}
	/**
	 * Set the Y component (second last; synonym for b).
	 * @param value The value to assign to the component.
	 */
	public void setY(long value) {
		components[ComponentLUT.getY(components.length)] = value;
	}

	/**
	 * @return The Z component (third last; synonym for c).
	 */
	public long getZ() {
		return components[ComponentLUT.getZ(components.length)];
	}
	/**
	 * Set the Z component (third last; synonym for c).
	 * @param value The value to assign to the component.
	 */
	public void setZ(long value) {
		components[ComponentLUT.getZ(components.length)] = value;
	}

	/**
	 * @return The W component (fourth last; synonym for d).
	 */
	public long getW() {
		return components[ComponentLUT.getW(components.length)];
	}
	/**
	 * Set the W component (fourth last; synonym for d).
	 * @param value The value to assign to the component.
	 */
	public void setW(long value) {
		components[ComponentLUT.getW(components.length)] = value;
	}

	/**
	 * @return The A component (last; synonym for x).
	 */
	public long getA() {
		return components[ComponentLUT.getA(components.length)];
	}
	/**
	 * Set the A component (last; synonym for x).
	 * @param value The value to assign to the component.
	 */
	public void setA(long value) {
		components[ComponentLUT.getA(components.length)] = value;
	}

	/**
	 * @return The B component (second last; synonym for y).
	 */
	public long getB() {
		return components[ComponentLUT.getB(components.length)];
	}
	/**
	 * Set the B component (second last; synonym for y).
	 * @param value The value to assign to the component.
	 */
	public void setB(long value) {
		components[ComponentLUT.getB(components.length)] = value;
	}

	/**
	 * @return The C component (third last; synonym for z).
	 */
	public long getC() {
		return components[ComponentLUT.getC(components.length)];
	}
	/**
	 * Set the C component (third last; synonym for z).
	 * @param value The value to assign to the component.
	 */
	public void setC(long value) {
		components[ComponentLUT.getC(components.length)] = value;
	}

	/**
	 * @return The D component (fourth last; synonym for w).
	 */
	public long getD() {
		return components[ComponentLUT.getD(components.length)];
	}
	/**
	 * Set the D component (fourth last; synonym for w).
	 * @param value The value to assign to the component.
	 */
	public void setD(long value) {
		components[ComponentLUT.getD(components.length)] = value;
	}

	/**
	 * @return The E component (fifth last; synonym for None).
	 */
	public long getE() {
		return components[ComponentLUT.getE(components.length)];
	}
	/**
	 * Set the E component (fifth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setE(long value) {
		components[ComponentLUT.getE(components.length)] = value;
	}

	/**
	 * @return The F component (sixth last; synonym for None).
	 */
	public long getF() {
		return components[ComponentLUT.getF(components.length)];
	}
	/**
	 * Set the F component (sixth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setF(long value) {
		components[ComponentLUT.getF(components.length)] = value;
	}

	/**
	 * @return The G component (seventh last; synonym for None).
	 */
	public long getG() {
		return components[ComponentLUT.getG(components.length)];
	}
	/**
	 * Set the G component (seventh last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setG(long value) {
		components[ComponentLUT.getG(components.length)] = value;
	}

	/**
	 * @return The H component (eighth last; synonym for None).
	 */
	public long getH() {
		return components[ComponentLUT.getH(components.length)];
	}
	/**
	 * Set the H component (eighth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setH(long value) {
		components[ComponentLUT.getH(components.length)] = value;
	}

	/**
	 * @return The I component (ninth last; synonym for None).
	 */
	public long getI() {
		return components[ComponentLUT.getI(components.length)];
	}
	/**
	 * Set the I component (ninth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setI(long value) {
		components[ComponentLUT.getI(components.length)] = value;
	}

	/**
	 * @return The J component (tenth last; synonym for None).
	 */
	public long getJ() {
		return components[ComponentLUT.getJ(components.length)];
	}
	/**
	 * Set the J component (tenth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setJ(long value) {
		components[ComponentLUT.getJ(components.length)] = value;
	}

	/**
	 * @return The T component (first; synonym for None).
	 */
	public long getT() {
		return components[ComponentLUT.getT(components.length)];
	}
	/**
	 * Set the T component (first; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setT(long value) {
		components[ComponentLUT.getT(components.length)] = value;
	}

	public VectorReal toReal() {
		VectorReal res = VectorReal.createEmpty(size());
		res.set(this);
		return res;
	}

	private int[] intComponents;
	public void fromIndex(Index ima) {
		int[] counter = ima.getCurrentCounter();
		for (int i = 0; i < components.length; i++)
			components[i] = counter[i];
	}
	public void fillIndex(Index ima) {
		ima.set(asIntArray());
	}
	public int[] asIntArray() {
		if (intComponents == null)
			intComponents = new int[components.length];
		for (int i = 0; i < components.length; i++)
			intComponents[i] = (int) components[i];
		return intComponents;
	}

    /**
     * Reinterpret the current value so it remains within the bounds of the
     * given shape. The value will be changed such that lower dimensions will
     * overflow into higher dimensions; thus this is ideal for traversing
     * scanline images.
     *
     * @param shape The shape to bound the value to
     */
	public void wrapInPlace(VectorInt shape) {
		long quotient = 0;
		// Iterate in reverse: x, y, z, w. Later dimensions depend on the result
		// of previous ones.
		for (int i = components.length - 1; i >= 0; i--) {
			long current = components[i] + quotient;
			components[i] = current % shape.components[i];
			quotient = current / shape.components[i];
		}
	}

	/**
	 * Add one to the fastest varying dimension, and then wrap according to the
	 * shape. This allows iteration over the pixels of an image with the given
	 * shape. Equivalent to adding one to the X axis and calling
	 * {@link #wrapInPlace(VectorInt) wrapInPlace}, but faster.
	 * 
	 * @param shape The dimensions of the image to iterate over.
	 */
	public void incr(VectorInt shape) {
		// Iterate in reverse: x, y, z, w. Later dimensions depend on the result
		// of previous ones.
		for (int i = components.length - 1; i >= 0; i--) {
			components[i] += 1;
			if (components[i] < shape.components[i])
				return;
			components[i] = 0;
		}
	}

	// ARITHMETIC

	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorInt addNew(long other) {
		VectorInt res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorInt addNew(double other) {
		VectorInt res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorInt addNew(VectorInt other) {
		VectorInt res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorInt addNew(VectorReal other) {
		VectorInt res = copy();
		return res.add(other);
	}

	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt add(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] + other;
		return this;
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt add(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] + (long)other;
		return this;
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt add(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] + other.components[i];
		}
		return this;
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt add(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] + (long)other.components[i];
		}
		return this;
	}

	/**
	 * Add two values, storing the result in this (third) instance.
	 *
	 * @param a The value to add.
	 * @param b The value to add to.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt addOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)(a.components[i] + b.components[i]);
		}
		return this;
	}
	/**
	 * Add two values, storing the result in this (third) instance.
	 *
	 * @param a The value to add.
	 * @param b The value to add to.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt addOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)a.components[i] + b.components[i];
		}
		return this;
	}
	/**
	 * Add two values, storing the result in this (third) instance.
	 *
	 * @param a The value to add.
	 * @param b The value to add to.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt addOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] + (long)b.components[i];
		}
		return this;
	}
	/**
	 * Add two values, storing the result in this (third) instance.
	 *
	 * @param a The value to add.
	 * @param b The value to add to.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt addOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] + b.components[i];
		}
		return this;
	}

	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorInt subNew(long other) {
		VectorInt res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorInt subNew(double other) {
		VectorInt res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorInt subNew(VectorInt other) {
		VectorInt res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorInt subNew(VectorReal other) {
		VectorInt res = copy();
		return res.sub(other);
	}

	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt sub(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] - other;
		return this;
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt sub(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] - (long)other;
		return this;
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt sub(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] - other.components[i];
		}
		return this;
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt sub(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] - (long)other.components[i];
		}
		return this;
	}

	/**
	 * Subtract two values, storing the result in this (third) instance.
	 *
	 * @param a The value to subtract.
	 * @param b The value to subtract from.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt subOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)(a.components[i] - b.components[i]);
		}
		return this;
	}
	/**
	 * Subtract two values, storing the result in this (third) instance.
	 *
	 * @param a The value to subtract.
	 * @param b The value to subtract from.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt subOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)a.components[i] - b.components[i];
		}
		return this;
	}
	/**
	 * Subtract two values, storing the result in this (third) instance.
	 *
	 * @param a The value to subtract.
	 * @param b The value to subtract from.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt subOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] - (long)b.components[i];
		}
		return this;
	}
	/**
	 * Subtract two values, storing the result in this (third) instance.
	 *
	 * @param a The value to subtract.
	 * @param b The value to subtract from.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt subOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] - b.components[i];
		}
		return this;
	}

	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorInt mulNew(long other) {
		VectorInt res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorInt mulNew(double other) {
		VectorInt res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorInt mulNew(VectorInt other) {
		VectorInt res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorInt mulNew(VectorReal other) {
		VectorInt res = copy();
		return res.mul(other);
	}

	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mul(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] * other;
		return this;
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mul(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] * (long)other;
		return this;
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mul(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] * other.components[i];
		}
		return this;
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mul(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] * (long)other.components[i];
		}
		return this;
	}

	/**
	 * Multiply two values, storing the result in this (third) instance.
	 *
	 * @param a The value to multiply.
	 * @param b The value to multiply with.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mulOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)(a.components[i] * b.components[i]);
		}
		return this;
	}
	/**
	 * Multiply two values, storing the result in this (third) instance.
	 *
	 * @param a The value to multiply.
	 * @param b The value to multiply with.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mulOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)a.components[i] * b.components[i];
		}
		return this;
	}
	/**
	 * Multiply two values, storing the result in this (third) instance.
	 *
	 * @param a The value to multiply.
	 * @param b The value to multiply with.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mulOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] * (long)b.components[i];
		}
		return this;
	}
	/**
	 * Multiply two values, storing the result in this (third) instance.
	 *
	 * @param a The value to multiply.
	 * @param b The value to multiply with.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mulOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] * b.components[i];
		}
		return this;
	}

	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorInt divNew(long other) {
		VectorInt res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorInt divNew(double other) {
		VectorInt res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorInt divNew(VectorInt other) {
		VectorInt res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorInt divNew(VectorReal other) {
		VectorInt res = copy();
		return res.div(other);
	}

	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt div(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] / other;
		return this;
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt div(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] / (long)other;
		return this;
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt div(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] / other.components[i];
		}
		return this;
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt div(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] / (long)other.components[i];
		}
		return this;
	}

	/**
	 * Divide two values, storing the result in this (third) instance.
	 *
	 * @param a The value to divide.
	 * @param b The value to divide by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt divOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)(a.components[i] / b.components[i]);
		}
		return this;
	}
	/**
	 * Divide two values, storing the result in this (third) instance.
	 *
	 * @param a The value to divide.
	 * @param b The value to divide by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt divOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)a.components[i] / b.components[i];
		}
		return this;
	}
	/**
	 * Divide two values, storing the result in this (third) instance.
	 *
	 * @param a The value to divide.
	 * @param b The value to divide by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt divOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] / (long)b.components[i];
		}
		return this;
	}
	/**
	 * Divide two values, storing the result in this (third) instance.
	 *
	 * @param a The value to divide.
	 * @param b The value to divide by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt divOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] / b.components[i];
		}
		return this;
	}

	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorInt modNew(long other) {
		VectorInt res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorInt modNew(double other) {
		VectorInt res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorInt modNew(VectorInt other) {
		VectorInt res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorInt modNew(VectorReal other) {
		VectorInt res = copy();
		return res.mod(other);
	}

	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mod(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] % other;
		return this;
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mod(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] % (long)other;
		return this;
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mod(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] % other.components[i];
		}
		return this;
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt mod(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] % (long)other.components[i];
		}
		return this;
	}

	/**
	 * Modulo two values, storing the result in this (third) instance.
	 *
	 * @param a The value to modulo.
	 * @param b The value to modulo by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt modOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)(a.components[i] % b.components[i]);
		}
		return this;
	}
	/**
	 * Modulo two values, storing the result in this (third) instance.
	 *
	 * @param a The value to modulo.
	 * @param b The value to modulo by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt modOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (long)a.components[i] % b.components[i];
		}
		return this;
	}
	/**
	 * Modulo two values, storing the result in this (third) instance.
	 *
	 * @param a The value to modulo.
	 * @param b The value to modulo by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt modOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] % (long)b.components[i];
		}
		return this;
	}
	/**
	 * Modulo two values, storing the result in this (third) instance.
	 *
	 * @param a The value to modulo.
	 * @param b The value to modulo by.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorInt modOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] % b.components[i];
		}
		return this;
	}

	// BOUNDING

	public VectorInt min(long other) {
		for (int i = 0; i < components.length; i++) {
			if (other < components[i])
				components[i] = other;
		}
		return this;
	}
	public VectorInt min(double other) {
		for (int i = 0; i < components.length; i++) {
			if (other < components[i])
				components[i] = (long)other;
		}
		return this;
	}
	public VectorInt min(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] < components[i])
				components[i] = other.components[i];
		}
		return this;
	}
	public VectorInt min(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] < components[i])
				components[i] = (long)other.components[i];
		}
		return this;
	}

	public VectorInt minNew(long other) {
		VectorInt res = copy();
		return res.min(other);
	}
	public VectorInt minNew(double other) {
		VectorInt res = copy();
		return res.min(other);
	}
	public VectorInt minNew(VectorInt other) {
		VectorInt res = copy();
		return res.min(other);
	}
	public VectorInt minNew(VectorReal other) {
		VectorInt res = copy();
		return res.min(other);
	}

	public VectorInt minOf(long a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt minOf(long a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = a;
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt minOf(double a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = (long)a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt minOf(double a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = (long)a;
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt minOf(long a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt minOf(double a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = (long)a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt minOf(long a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = a;
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt minOf(double a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = (long)a;
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt minOf(VectorInt a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt minOf(VectorInt a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = a.components[i];
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt minOf(VectorReal a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = (long)a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt minOf(VectorReal a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = (long)a.components[i];
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt minOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt minOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = a.components[i];
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt minOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = (long)a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt minOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = (long)a.components[i];
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}

	public VectorInt max(long other) {
		for (int i = 0; i < components.length; i++) {
			if (other > components[i])
				components[i] = other;
		}
		return this;
	}
	public VectorInt max(double other) {
		for (int i = 0; i < components.length; i++) {
			if (other > components[i])
				components[i] = (long)other;
		}
		return this;
	}
	public VectorInt max(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] > components[i])
				components[i] = other.components[i];
		}
		return this;
	}
	public VectorInt max(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] > components[i])
				components[i] = (long)other.components[i];
		}
		return this;
	}

	public VectorInt maxNew(long other) {
		VectorInt res = copy();
		return res.max(other);
	}
	public VectorInt maxNew(double other) {
		VectorInt res = copy();
		return res.max(other);
	}
	public VectorInt maxNew(VectorInt other) {
		VectorInt res = copy();
		return res.max(other);
	}
	public VectorInt maxNew(VectorReal other) {
		VectorInt res = copy();
		return res.max(other);
	}

	public VectorInt maxOf(long a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt maxOf(long a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = a;
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt maxOf(double a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = (long)a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt maxOf(double a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = (long)a;
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt maxOf(long a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(double a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = (long)a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(long a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = a;
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(double a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = (long)a;
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(VectorInt a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt maxOf(VectorInt a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = a.components[i];
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt maxOf(VectorReal a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = (long)a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorInt maxOf(VectorReal a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = (long)a.components[i];
			else
				components[i] = (long)b;
		}
		return this;
	}
	public VectorInt maxOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = a.components[i];
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = (long)a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorInt maxOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = (long)a.components[i];
			else
				components[i] = (long)b.components[i];
		}
		return this;
	}

	public VectorInt clamp(long min, long max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min)
				components[i] = min;
			else if (components[i] > max)
				components[i] = max;
		}
		return this;
	}
	public VectorInt clamp(double min, double max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min)
				components[i] = (long)min;
			else if (components[i] > max)
				components[i] = (long)max;
		}
		return this;
	}
	public VectorInt clamp(VectorInt min, VectorInt max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min.components[i])
				components[i] = min.components[i];
			else if (components[i] > max.components[i])
				components[i] = max.components[i];
		}
		return this;
	}
	public VectorInt clamp(VectorReal min, VectorReal max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min.components[i])
				components[i] = (long)min.components[i];
			else if (components[i] > max.components[i])
				components[i] = (long)max.components[i];
		}
		return this;
	}

	public VectorInt clampNew(long min, long max) {
		VectorInt res = copy();
		return res.clamp(min, max);
	}
	public VectorInt clampNew(double min, double max) {
		VectorInt res = copy();
		return res.clamp(min, max);
	}
	public VectorInt clampNew(VectorInt min, VectorInt max) {
		VectorInt res = copy();
		return res.clamp(min, max);
	}
	public VectorInt clampNew(VectorReal min, VectorReal max) {
		VectorInt res = copy();
		return res.clamp(min, max);
	}

	public boolean equals(VectorInt other) {
		try {
			return Arrays.equals(components, other.components);
		} catch (NullPointerException e) {
			return false;
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (VectorInt.class != obj.getClass())
			return false;
		VectorInt other = (VectorInt) obj;
		return Arrays.equals(components, other.components);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(components);
	}

}
