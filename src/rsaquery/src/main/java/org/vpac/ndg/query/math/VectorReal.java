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
public class VectorReal implements Serializable {

	private static final long serialVersionUID = 1L;
	double[] components;

	protected VectorReal() {
	}
	protected VectorReal(int size) {
		components = new double[size];
	}

	public static VectorReal createEmpty(int capacity) {
		return new VectorReal(capacity);
	}

	public static VectorReal createEmpty(int capacity, double fill) {
		VectorReal res = new VectorReal(capacity);
		for (int i = 0; i < res.components.length; i++) {
			res.components[i] = fill;
		}
		return res;
	}

	public static VectorReal create(double... components) {
		if (components.length == 0 || components.length > 4) {
			throw new IllegalArgumentException(
					"Vectors must have from 1-4 components.");
		}
		VectorReal res = new VectorReal();
		res.components = components.clone();
		return res;
	}

	public static VectorReal fromInt(int... components) {
		if (components.length == 0 || components.length > 4) {
			throw new IllegalArgumentException(
					"Vectors must have from 1-4 components.");
		}
		VectorReal res = createEmpty(components.length);
		for (int i = 0; i < components.length; i++)
			res.components[i] = components[i];
		return res;
	}

	public VectorReal copy() {
		VectorReal res = new VectorReal();
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
		for (double c : components) {
			if (first)
				first = false;
			else
				buf.append(", ");
			buf.append(c);
		}
		buf.append(")");
		return buf.toString();
	}

	public void set(double other) {
		for (int i = 0; i < components.length; i++)
			components[i] = other;
	}
	public void set(VectorReal other) {
		for (int i = 0; i < components.length; i++)
			components[i] = other.components[i];
	}
	public void set(VectorInt other) {
		for (int i = 0; i < components.length; i++)
			components[i] = (double)other.components[i];
	}

	public double[] asArray() {
		return components;
	}

	public double volume() {
		double vol = 1;
		for (int i = 0; i < components.length; i++) {
			vol *= components[i];
		}
		return vol;
	}

	public int size() {
		return components.length;
	}
	public double get(int i) {
		return components[i];
	}
	public void set(int i, double other) {
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
	public double getX() {
		return components[ComponentLUT.getX(components.length)];
	}
	/**
	 * Set the X component (last; synonym for a).
	 * @param value The value to assign to the component.
	 */
	public void setX(double value) {
		components[ComponentLUT.getX(components.length)] = value;
	}

	/**
	 * @return The Y component (second last; synonym for b).
	 */
	public double getY() {
		return components[ComponentLUT.getY(components.length)];
	}
	/**
	 * Set the Y component (second last; synonym for b).
	 * @param value The value to assign to the component.
	 */
	public void setY(double value) {
		components[ComponentLUT.getY(components.length)] = value;
	}

	/**
	 * @return The Z component (third last; synonym for c).
	 */
	public double getZ() {
		return components[ComponentLUT.getZ(components.length)];
	}
	/**
	 * Set the Z component (third last; synonym for c).
	 * @param value The value to assign to the component.
	 */
	public void setZ(double value) {
		components[ComponentLUT.getZ(components.length)] = value;
	}

	/**
	 * @return The W component (fourth last; synonym for d).
	 */
	public double getW() {
		return components[ComponentLUT.getW(components.length)];
	}
	/**
	 * Set the W component (fourth last; synonym for d).
	 * @param value The value to assign to the component.
	 */
	public void setW(double value) {
		components[ComponentLUT.getW(components.length)] = value;
	}

	/**
	 * @return The A component (last; synonym for x).
	 */
	public double getA() {
		return components[ComponentLUT.getA(components.length)];
	}
	/**
	 * Set the A component (last; synonym for x).
	 * @param value The value to assign to the component.
	 */
	public void setA(double value) {
		components[ComponentLUT.getA(components.length)] = value;
	}

	/**
	 * @return The B component (second last; synonym for y).
	 */
	public double getB() {
		return components[ComponentLUT.getB(components.length)];
	}
	/**
	 * Set the B component (second last; synonym for y).
	 * @param value The value to assign to the component.
	 */
	public void setB(double value) {
		components[ComponentLUT.getB(components.length)] = value;
	}

	/**
	 * @return The C component (third last; synonym for z).
	 */
	public double getC() {
		return components[ComponentLUT.getC(components.length)];
	}
	/**
	 * Set the C component (third last; synonym for z).
	 * @param value The value to assign to the component.
	 */
	public void setC(double value) {
		components[ComponentLUT.getC(components.length)] = value;
	}

	/**
	 * @return The D component (fourth last; synonym for w).
	 */
	public double getD() {
		return components[ComponentLUT.getD(components.length)];
	}
	/**
	 * Set the D component (fourth last; synonym for w).
	 * @param value The value to assign to the component.
	 */
	public void setD(double value) {
		components[ComponentLUT.getD(components.length)] = value;
	}

	/**
	 * @return The E component (fifth last; synonym for None).
	 */
	public double getE() {
		return components[ComponentLUT.getE(components.length)];
	}
	/**
	 * Set the E component (fifth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setE(double value) {
		components[ComponentLUT.getE(components.length)] = value;
	}

	/**
	 * @return The F component (sixth last; synonym for None).
	 */
	public double getF() {
		return components[ComponentLUT.getF(components.length)];
	}
	/**
	 * Set the F component (sixth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setF(double value) {
		components[ComponentLUT.getF(components.length)] = value;
	}

	/**
	 * @return The G component (seventh last; synonym for None).
	 */
	public double getG() {
		return components[ComponentLUT.getG(components.length)];
	}
	/**
	 * Set the G component (seventh last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setG(double value) {
		components[ComponentLUT.getG(components.length)] = value;
	}

	/**
	 * @return The H component (eighth last; synonym for None).
	 */
	public double getH() {
		return components[ComponentLUT.getH(components.length)];
	}
	/**
	 * Set the H component (eighth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setH(double value) {
		components[ComponentLUT.getH(components.length)] = value;
	}

	/**
	 * @return The I component (ninth last; synonym for None).
	 */
	public double getI() {
		return components[ComponentLUT.getI(components.length)];
	}
	/**
	 * Set the I component (ninth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setI(double value) {
		components[ComponentLUT.getI(components.length)] = value;
	}

	/**
	 * @return The J component (tenth last; synonym for None).
	 */
	public double getJ() {
		return components[ComponentLUT.getJ(components.length)];
	}
	/**
	 * Set the J component (tenth last; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setJ(double value) {
		components[ComponentLUT.getJ(components.length)] = value;
	}

	/**
	 * @return The T component (first; synonym for None).
	 */
	public double getT() {
		return components[ComponentLUT.getT(components.length)];
	}
	/**
	 * Set the T component (first; synonym for None).
	 * @param value The value to assign to the component.
	 */
	public void setT(double value) {
		components[ComponentLUT.getT(components.length)] = value;
	}

	public VectorInt toInt() {
		VectorInt res = VectorInt.createEmpty(size());
		res.set(this);
		return res;
	}

	/**
	 * Set this vector to the same location as an Index.
	 * 
	 * <p>
	 * Note that the Index is essentially an array of integers, which natually
	 * address a discrete set of pixels. But a real vector is ambiguous about
	 * the coordinates of the "centre" of a pixel. The convention used in
	 * rsaquery is (0.0, 0.0) - <em>not</em> (0.5, 0.5) as in some other imaging
	 * toolkits.
	 * </p>
	 * 
	 * @param ima
	 */
	public void fromIndex(Index ima) {
		int[] counter = ima.getCurrentCounter();
		for (int i = 0; i < components.length; i++)
			components[i] = counter[i];
	}

	// ARITHMETIC

	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorReal addNew(long other) {
		VectorReal res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorReal addNew(double other) {
		VectorReal res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorReal addNew(VectorInt other) {
		VectorReal res = copy();
		return res.add(other);
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return The result as a new object.
	 */
	public VectorReal addNew(VectorReal other) {
		VectorReal res = copy();
		return res.add(other);
	}

	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal add(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] + (double)other;
		return this;
	}
	/**
	 * Add this object to a value.
	 *
	 * @param other The value to add.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal add(double other) {
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
	public VectorReal add(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] + (double)other.components[i];
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
	public VectorReal add(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] + other.components[i];
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
	public VectorReal addOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (a.components[i] + b.components[i]);
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
	public VectorReal addOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] + (double)b.components[i];
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
	public VectorReal addOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] + b.components[i];
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
	public VectorReal addOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] + (double)b.components[i];
		}
		return this;
	}

	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorReal subNew(long other) {
		VectorReal res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorReal subNew(double other) {
		VectorReal res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorReal subNew(VectorInt other) {
		VectorReal res = copy();
		return res.sub(other);
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return The result as a new object.
	 */
	public VectorReal subNew(VectorReal other) {
		VectorReal res = copy();
		return res.sub(other);
	}

	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal sub(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] - (double)other;
		return this;
	}
	/**
	 * Subtract this object from a value.
	 *
	 * @param other The value to subtract.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal sub(double other) {
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
	public VectorReal sub(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] - (double)other.components[i];
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
	public VectorReal sub(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] - other.components[i];
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
	public VectorReal subOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (a.components[i] - b.components[i]);
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
	public VectorReal subOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] - (double)b.components[i];
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
	public VectorReal subOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] - b.components[i];
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
	public VectorReal subOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] - (double)b.components[i];
		}
		return this;
	}

	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorReal mulNew(long other) {
		VectorReal res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorReal mulNew(double other) {
		VectorReal res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorReal mulNew(VectorInt other) {
		VectorReal res = copy();
		return res.mul(other);
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return The result as a new object.
	 */
	public VectorReal mulNew(VectorReal other) {
		VectorReal res = copy();
		return res.mul(other);
	}

	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal mul(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] * (double)other;
		return this;
	}
	/**
	 * Multiply this object with a value.
	 *
	 * @param other The value to multiply.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal mul(double other) {
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
	public VectorReal mul(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] * (double)other.components[i];
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
	public VectorReal mul(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] * other.components[i];
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
	public VectorReal mulOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (a.components[i] * b.components[i]);
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
	public VectorReal mulOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] * (double)b.components[i];
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
	public VectorReal mulOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] * b.components[i];
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
	public VectorReal mulOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] * (double)b.components[i];
		}
		return this;
	}

	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorReal divNew(long other) {
		VectorReal res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorReal divNew(double other) {
		VectorReal res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorReal divNew(VectorInt other) {
		VectorReal res = copy();
		return res.div(other);
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return The result as a new object.
	 */
	public VectorReal divNew(VectorReal other) {
		VectorReal res = copy();
		return res.div(other);
	}

	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal div(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] / (double)other;
		return this;
	}
	/**
	 * Divide this object by a value.
	 *
	 * @param other The value to divide.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal div(double other) {
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
	public VectorReal div(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] / (double)other.components[i];
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
	public VectorReal div(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] / other.components[i];
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
	public VectorReal divOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (a.components[i] / b.components[i]);
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
	public VectorReal divOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] / (double)b.components[i];
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
	public VectorReal divOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] / b.components[i];
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
	public VectorReal divOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] / (double)b.components[i];
		}
		return this;
	}

	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorReal modNew(long other) {
		VectorReal res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorReal modNew(double other) {
		VectorReal res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorReal modNew(VectorInt other) {
		VectorReal res = copy();
		return res.mod(other);
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return The result as a new object.
	 */
	public VectorReal modNew(VectorReal other) {
		VectorReal res = copy();
		return res.mod(other);
	}

	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal mod(long other) {
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] % (double)other;
		return this;
	}
	/**
	 * Modulo this object by a value.
	 *
	 * @param other The value to modulo.
	 * @return A reference to this object. Note that this method does not create
	 *         a new instance.
	 */
	public VectorReal mod(double other) {
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
	public VectorReal mod(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] % (double)other.components[i];
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
	public VectorReal mod(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			components[i] = components[i] % other.components[i];
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
	public VectorReal modOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (a.components[i] % b.components[i]);
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
	public VectorReal modOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = a.components[i] % (double)b.components[i];
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
	public VectorReal modOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] % b.components[i];
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
	public VectorReal modOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			components[i] = (double)a.components[i] % (double)b.components[i];
		}
		return this;
	}

	// BOUNDING

	public VectorReal min(long other) {
		for (int i = 0; i < components.length; i++) {
			if (other < components[i])
				components[i] = (double)other;
		}
		return this;
	}
	public VectorReal min(double other) {
		for (int i = 0; i < components.length; i++) {
			if (other < components[i])
				components[i] = other;
		}
		return this;
	}
	public VectorReal min(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] < components[i])
				components[i] = (double)other.components[i];
		}
		return this;
	}
	public VectorReal min(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] < components[i])
				components[i] = other.components[i];
		}
		return this;
	}

	public VectorReal minNew(long other) {
		VectorReal res = copy();
		return res.min(other);
	}
	public VectorReal minNew(double other) {
		VectorReal res = copy();
		return res.min(other);
	}
	public VectorReal minNew(VectorInt other) {
		VectorReal res = copy();
		return res.min(other);
	}
	public VectorReal minNew(VectorReal other) {
		VectorReal res = copy();
		return res.min(other);
	}

	public VectorReal minOf(long a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = (double)a;
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal minOf(long a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = (double)a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal minOf(double a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = a;
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal minOf(double a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b)
				components[i] = a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal minOf(long a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = (double)a;
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal minOf(double a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = a;
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal minOf(long a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = (double)a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal minOf(double a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a < b.components[i])
				components[i] = a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal minOf(VectorInt a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = (double)a.components[i];
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal minOf(VectorInt a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = (double)a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal minOf(VectorReal a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = a.components[i];
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal minOf(VectorReal a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b)
				components[i] = a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal minOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = (double)a.components[i];
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal minOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = (double)a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal minOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = a.components[i];
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal minOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] < b.components[i])
				components[i] = a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}

	public VectorReal max(long other) {
		for (int i = 0; i < components.length; i++) {
			if (other > components[i])
				components[i] = (double)other;
		}
		return this;
	}
	public VectorReal max(double other) {
		for (int i = 0; i < components.length; i++) {
			if (other > components[i])
				components[i] = other;
		}
		return this;
	}
	public VectorReal max(VectorInt other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] > components[i])
				components[i] = (double)other.components[i];
		}
		return this;
	}
	public VectorReal max(VectorReal other) {
		for (int i = 0; i < components.length; i++) {
			if (other.components[i] > components[i])
				components[i] = other.components[i];
		}
		return this;
	}

	public VectorReal maxNew(long other) {
		VectorReal res = copy();
		return res.max(other);
	}
	public VectorReal maxNew(double other) {
		VectorReal res = copy();
		return res.max(other);
	}
	public VectorReal maxNew(VectorInt other) {
		VectorReal res = copy();
		return res.max(other);
	}
	public VectorReal maxNew(VectorReal other) {
		VectorReal res = copy();
		return res.max(other);
	}

	public VectorReal maxOf(long a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = (double)a;
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal maxOf(long a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = (double)a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal maxOf(double a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = a;
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal maxOf(double a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b)
				components[i] = a;
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal maxOf(long a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = (double)a;
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(double a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = a;
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(long a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = (double)a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(double a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a > b.components[i])
				components[i] = a;
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(VectorInt a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = (double)a.components[i];
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal maxOf(VectorInt a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = (double)a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal maxOf(VectorReal a, long b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = a.components[i];
			else
				components[i] = (double)b;
		}
		return this;
	}
	public VectorReal maxOf(VectorReal a, double b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b)
				components[i] = a.components[i];
			else
				components[i] = b;
		}
		return this;
	}
	public VectorReal maxOf(VectorInt a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = (double)a.components[i];
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(VectorInt a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = (double)a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(VectorReal a, VectorInt b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = a.components[i];
			else
				components[i] = (double)b.components[i];
		}
		return this;
	}
	public VectorReal maxOf(VectorReal a, VectorReal b) {
		for (int i = 0; i < components.length; i++) {
			if (a.components[i] > b.components[i])
				components[i] = a.components[i];
			else
				components[i] = b.components[i];
		}
		return this;
	}

	public VectorReal clamp(long min, long max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min)
				components[i] = (double)min;
			else if (components[i] > max)
				components[i] = (double)max;
		}
		return this;
	}
	public VectorReal clamp(double min, double max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min)
				components[i] = min;
			else if (components[i] > max)
				components[i] = max;
		}
		return this;
	}
	public VectorReal clamp(VectorInt min, VectorInt max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min.components[i])
				components[i] = (double)min.components[i];
			else if (components[i] > max.components[i])
				components[i] = (double)max.components[i];
		}
		return this;
	}
	public VectorReal clamp(VectorReal min, VectorReal max) {
		for (int i = 0; i < components.length; i++) {
			if (components[i] < min.components[i])
				components[i] = min.components[i];
			else if (components[i] > max.components[i])
				components[i] = max.components[i];
		}
		return this;
	}

	public VectorReal clampNew(long min, long max) {
		VectorReal res = copy();
		return res.clamp(min, max);
	}
	public VectorReal clampNew(double min, double max) {
		VectorReal res = copy();
		return res.clamp(min, max);
	}
	public VectorReal clampNew(VectorInt min, VectorInt max) {
		VectorReal res = copy();
		return res.clamp(min, max);
	}
	public VectorReal clampNew(VectorReal min, VectorReal max) {
		VectorReal res = copy();
		return res.clamp(min, max);
	}

	public boolean equals(VectorReal other) {
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
		if (VectorReal.class != obj.getClass())
			return false;
		VectorReal other = (VectorReal) obj;
		return Arrays.equals(components, other.components);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(components);
	}

}
