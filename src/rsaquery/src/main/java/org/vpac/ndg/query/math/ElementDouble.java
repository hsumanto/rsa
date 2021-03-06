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

// THIS IS GENERATED CODE. Do not modify this file. See ElementX_gen.py.

package org.vpac.ndg.query.math;

/**
 * A pixel that stores a double value.
 *
 * <p><img src="doc-files/Element_class.png" /></p>
 *
 * @author Alex Fraser
 */
public class ElementDouble implements ScalarElement {
	private double value;
	private boolean valid;

	/* Components array makes this scalar element look like a vector. */
	private ElementDouble[] components;

	/**
	 * Create a new ElementDouble, initalised to zero.
	 */
	public ElementDouble() {
		this((double)0);
	}
	/**
	 * Create a new ElementDouble.
	 * @param value The initial value for the element.
	 */
	public ElementDouble(double value) {
		this.value = value;
		this.valid = true;
		components = new ElementDouble[] { this };
	}
	@Override
	public ElementDouble copy() {
		ElementDouble res = new ElementDouble(value);
		res.valid = valid;
		return res;
	}
	@Override
	public ElementDouble[] getComponents() {
		return components;
	}
	@Override
	public int size() {
		return components.length;
	}

	@Override
	public boolean isValid() {
		return valid;
	}
	@Override
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	@Override
	public void setValid(Element<?> mask) {
		this.valid = mask.isValid();
	}
	@Override
	public void setValidIfValid(Element<?> mask) {
		if (mask.isValid())
			this.valid = true;
	}

	@Override
	public Double getValue() {
		return value;
	}

	// CASTING

	@Override
	public ElementDouble set(Element<?> value) {
		this.value = ((ScalarElement)value).doubleValue();
		this.valid = value.isValid();
		return this;
	}
	@Override
	public ElementDouble set(Number value) {
		this.value = value.doubleValue();
		this.valid = true;
		return this;
	}

	@Override
	public ElementDouble minimise() {
		this.value = Double.NEGATIVE_INFINITY;
		return this;
	}
	@Override
	public ElementDouble maximise() {
		this.value = Double.POSITIVE_INFINITY;
		return this;
	}

	@Override
	public byte byteValue() {
		return (byte)value;
	}
	@Override
	public ElementDouble set(byte value) {
		this.value = (double)value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementByte asByte() {
		ElementByte res = new ElementByte(this.byteValue());
		res.setValid(valid);
		return res;
	}

	@Override
	public short shortValue() {
		return (short)value;
	}
	@Override
	public ElementDouble set(short value) {
		this.value = (double)value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementShort asShort() {
		ElementShort res = new ElementShort(this.shortValue());
		res.setValid(valid);
		return res;
	}

	@Override
	public int intValue() {
		return (int)value;
	}
	@Override
	public ElementDouble set(int value) {
		this.value = (double)value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementInt asInt() {
		ElementInt res = new ElementInt(this.intValue());
		res.setValid(valid);
		return res;
	}

	@Override
	public long longValue() {
		return (long)value;
	}
	@Override
	public ElementDouble set(long value) {
		this.value = (double)value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementLong asLong() {
		ElementLong res = new ElementLong(this.longValue());
		res.setValid(valid);
		return res;
	}

	@Override
	public float floatValue() {
		return (float)value;
	}
	@Override
	public ElementDouble set(float value) {
		this.value = (double)value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementFloat asFloat() {
		ElementFloat res = new ElementFloat(this.floatValue());
		res.setValid(valid);
		return res;
	}

	@Override
	public double doubleValue() {
		return value;
	}
	@Override
	public ElementDouble set(double value) {
		this.value = value;
		this.valid = true;
		return this;
	}
	@Override
	public ElementDouble asDouble() {
		ElementDouble res = new ElementDouble(this.doubleValue());
		res.setValid(valid);
		return res;
	}

	// ARITHMETIC

	@Override
	public ElementDouble add(long other) {
		try {
			value = (double)(value + (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble add(double other) {
		try {
			value = (double)(value + other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if other is a vector.
	 */
	@Override
	public ElementDouble add(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		try {
			value = (double)(value + ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble addIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value + (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if mask is a vector.
	 */
	@Override
	public ElementDouble addIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value + other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble addIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		try {
			value = (double)(value + ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}
	@Override
	public ElementDouble addIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		try {
			value = (double)(value + ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}

	@Override
	public ElementDouble addNew(long other) {
		ElementDouble res = copy();
		return res.add(other);
	}
	@Override
	public ElementDouble addNew(double other) {
		ElementDouble res = copy();
		return res.add(other);
	}
	@Override
	public ElementDouble addNew(Element<?> other) {
		ElementDouble res = copy();
		return res.add(other);
	}

	@Override
	public ElementDouble addNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.addIfValid(other, mask);
	}
	@Override
	public ElementDouble addNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.addIfValid(other, mask);
	}
	@Override
	public ElementDouble addNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.addIfValid(other);
	}
	@Override
	public ElementDouble addNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.addIfValid(other, mask);
	}

	@Override
	public ElementDouble addOf(long a, long b) {
		try {
			value = (double)(a + b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble addOf(double a, long b) {
		try {
			value = (double)(a + (double)b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble addOf(long a, double b) {
		try {
			value = (double)((double)a + b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble addOf(double a, double b) {
		try {
			value = (double)((double)a + b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble addOf(Element<?> a, long b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av + b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble addOf(long a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a + bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble addOf(Element<?> a, double b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av + b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble addOf(double a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a + bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble addOf(Element<?> a, Element<?> b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(av + bv);
			valid = b.isValid() && a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble addOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			addOf(a, b);
		return this;
	}
	@Override
	public ElementDouble addOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			addOf(a, b);
		return this;
	}

	@Override
	public ElementDouble sub(long other) {
		try {
			value = (double)(value - (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble sub(double other) {
		try {
			value = (double)(value - other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if other is a vector.
	 */
	@Override
	public ElementDouble sub(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		try {
			value = (double)(value - ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble subIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value - (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if mask is a vector.
	 */
	@Override
	public ElementDouble subIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value - other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble subIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		try {
			value = (double)(value - ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}
	@Override
	public ElementDouble subIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		try {
			value = (double)(value - ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}

	@Override
	public ElementDouble subNew(long other) {
		ElementDouble res = copy();
		return res.sub(other);
	}
	@Override
	public ElementDouble subNew(double other) {
		ElementDouble res = copy();
		return res.sub(other);
	}
	@Override
	public ElementDouble subNew(Element<?> other) {
		ElementDouble res = copy();
		return res.sub(other);
	}

	@Override
	public ElementDouble subNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.subIfValid(other, mask);
	}
	@Override
	public ElementDouble subNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.subIfValid(other, mask);
	}
	@Override
	public ElementDouble subNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.subIfValid(other);
	}
	@Override
	public ElementDouble subNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.subIfValid(other, mask);
	}

	@Override
	public ElementDouble subOf(long a, long b) {
		try {
			value = (double)(a - b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble subOf(double a, long b) {
		try {
			value = (double)(a - (double)b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble subOf(long a, double b) {
		try {
			value = (double)((double)a - b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble subOf(double a, double b) {
		try {
			value = (double)((double)a - b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble subOf(Element<?> a, long b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av - b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble subOf(long a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a - bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble subOf(Element<?> a, double b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av - b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble subOf(double a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a - bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble subOf(Element<?> a, Element<?> b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(av - bv);
			valid = b.isValid() && a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble subOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			subOf(a, b);
		return this;
	}
	@Override
	public ElementDouble subOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			subOf(a, b);
		return this;
	}

	@Override
	public ElementDouble mul(long other) {
		try {
			value = (double)(value * (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mul(double other) {
		try {
			value = (double)(value * other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if other is a vector.
	 */
	@Override
	public ElementDouble mul(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		try {
			value = (double)(value * ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble mulIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value * (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if mask is a vector.
	 */
	@Override
	public ElementDouble mulIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value * other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mulIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		try {
			value = (double)(value * ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}
	@Override
	public ElementDouble mulIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		try {
			value = (double)(value * ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}

	@Override
	public ElementDouble mulNew(long other) {
		ElementDouble res = copy();
		return res.mul(other);
	}
	@Override
	public ElementDouble mulNew(double other) {
		ElementDouble res = copy();
		return res.mul(other);
	}
	@Override
	public ElementDouble mulNew(Element<?> other) {
		ElementDouble res = copy();
		return res.mul(other);
	}

	@Override
	public ElementDouble mulNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.mulIfValid(other, mask);
	}
	@Override
	public ElementDouble mulNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.mulIfValid(other, mask);
	}
	@Override
	public ElementDouble mulNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.mulIfValid(other);
	}
	@Override
	public ElementDouble mulNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.mulIfValid(other, mask);
	}

	@Override
	public ElementDouble mulOf(long a, long b) {
		try {
			value = (double)(a * b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mulOf(double a, long b) {
		try {
			value = (double)(a * (double)b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mulOf(long a, double b) {
		try {
			value = (double)((double)a * b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mulOf(double a, double b) {
		try {
			value = (double)((double)a * b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble mulOf(Element<?> a, long b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av * b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble mulOf(long a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a * bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble mulOf(Element<?> a, double b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av * b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble mulOf(double a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a * bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble mulOf(Element<?> a, Element<?> b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(av * bv);
			valid = b.isValid() && a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble mulOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			mulOf(a, b);
		return this;
	}
	@Override
	public ElementDouble mulOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			mulOf(a, b);
		return this;
	}

	@Override
	public ElementDouble div(long other) {
		try {
			value = (double)(value / (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble div(double other) {
		try {
			value = (double)(value / other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if other is a vector.
	 */
	@Override
	public ElementDouble div(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		try {
			value = (double)(value / ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble divIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value / (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if mask is a vector.
	 */
	@Override
	public ElementDouble divIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value / other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble divIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		try {
			value = (double)(value / ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}
	@Override
	public ElementDouble divIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		try {
			value = (double)(value / ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}

	@Override
	public ElementDouble divNew(long other) {
		ElementDouble res = copy();
		return res.div(other);
	}
	@Override
	public ElementDouble divNew(double other) {
		ElementDouble res = copy();
		return res.div(other);
	}
	@Override
	public ElementDouble divNew(Element<?> other) {
		ElementDouble res = copy();
		return res.div(other);
	}

	@Override
	public ElementDouble divNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.divIfValid(other, mask);
	}
	@Override
	public ElementDouble divNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.divIfValid(other, mask);
	}
	@Override
	public ElementDouble divNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.divIfValid(other);
	}
	@Override
	public ElementDouble divNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.divIfValid(other, mask);
	}

	@Override
	public ElementDouble divOf(long a, long b) {
		try {
			value = (double)(a / b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble divOf(double a, long b) {
		try {
			value = (double)(a / (double)b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble divOf(long a, double b) {
		try {
			value = (double)((double)a / b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble divOf(double a, double b) {
		try {
			value = (double)((double)a / b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble divOf(Element<?> a, long b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av / b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble divOf(long a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a / bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble divOf(Element<?> a, double b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av / b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble divOf(double a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a / bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble divOf(Element<?> a, Element<?> b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(av / bv);
			valid = b.isValid() && a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble divOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			divOf(a, b);
		return this;
	}
	@Override
	public ElementDouble divOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			divOf(a, b);
		return this;
	}

	@Override
	public ElementDouble mod(long other) {
		try {
			value = (double)(value % (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble mod(double other) {
		try {
			value = (double)(value % other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if other is a vector.
	 */
	@Override
	public ElementDouble mod(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		try {
			value = (double)(value % ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble modIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value % (double)other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @throws ClassCastException if mask is a vector.
	 */
	@Override
	public ElementDouble modIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		try {
			value = (double)(value % other);
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble modIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		try {
			value = (double)(value % ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}
	@Override
	public ElementDouble modIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		try {
			value = (double)(value % ((ScalarElement)other).doubleValue());
		} catch (ArithmeticException e) {
			// do nothing.
		}
		return this;
	}

	@Override
	public ElementDouble modNew(long other) {
		ElementDouble res = copy();
		return res.mod(other);
	}
	@Override
	public ElementDouble modNew(double other) {
		ElementDouble res = copy();
		return res.mod(other);
	}
	@Override
	public ElementDouble modNew(Element<?> other) {
		ElementDouble res = copy();
		return res.mod(other);
	}

	@Override
	public ElementDouble modNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.modIfValid(other, mask);
	}
	@Override
	public ElementDouble modNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.modIfValid(other, mask);
	}
	@Override
	public ElementDouble modNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.modIfValid(other);
	}
	@Override
	public ElementDouble modNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.modIfValid(other, mask);
	}

	@Override
	public ElementDouble modOf(long a, long b) {
		try {
			value = (double)(a % b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble modOf(double a, long b) {
		try {
			value = (double)(a % (double)b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble modOf(long a, double b) {
		try {
			value = (double)((double)a % b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	@Override
	public ElementDouble modOf(double a, double b) {
		try {
			value = (double)((double)a % b);
			valid = true;
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble modOf(Element<?> a, long b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av % b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble modOf(long a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a % bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble modOf(Element<?> a, double b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			value = (double)(av % b);
			valid = a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble modOf(double a, Element<?> b) {
		try {
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(a % bv);
			valid = b.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble modOf(Element<?> a, Element<?> b) {
		try {
			double av = ((ScalarElement)a).doubleValue();
			double bv = ((ScalarElement)b).doubleValue();
			value = (double)(av % bv);
			valid = b.isValid() && a.isValid();
		} catch (ArithmeticException e) {
			this.setValid(false);
		}
		return this;
	}

	@Override
	public ElementDouble modOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			modOf(a, b);
		return this;
	}
	@Override
	public ElementDouble modOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			modOf(a, b);
		return this;
	}

	// BOUNDING

	@Override
	public ElementDouble min(long other) {
		if (other < value)
			value = (double)other;
		return this;
	}
	@Override
	public ElementDouble min(double other) {
		if (other < value)
			value = other;
		return this;
	}
	@Override
	public ElementDouble min(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		if (((ScalarElement)other).doubleValue() < value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}

	@Override
	public ElementDouble minIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		if (other < value)
			value = (double)other;
		return this;
	}
	@Override
	public ElementDouble minIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		if (other < value)
			value = other;
		return this;
	}
	@Override
	public ElementDouble minIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		if (((ScalarElement)other).doubleValue() < value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}
	@Override
	public ElementDouble minIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		if (((ScalarElement)other).doubleValue() < value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}

	@Override
	public ElementDouble minNew(long other) {
		ElementDouble res = copy();
		return res.min(other);
	}
	@Override
	public ElementDouble minNew(double other) {
		ElementDouble res = copy();
		return res.min(other);
	}
	@Override
	public ElementDouble minNew(Element<?> other) {
		ElementDouble res = copy();
		return res.min(other);
	}

	@Override
	public ElementDouble minNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.minIfValid(other, mask);
	}
	@Override
	public ElementDouble minNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.minIfValid(other, mask);
	}
	@Override
	public ElementDouble minNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.minIfValid(other);
	}
	@Override
	public ElementDouble minNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.minIfValid(other, mask);
	}

	@Override
	public ElementDouble minOf(long a, long b) {
		if (a < b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble minOf(double a, long b) {
		if (a < b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble minOf(long a, double b) {
		if (a < b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble minOf(double a, double b) {
		if (a < b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble minOf(Element<?> a, long b) {
		double av = ((ScalarElement)a).doubleValue();
		if (av < b)
			value = (double)av;
		else
			value = (double)b;
		valid = a.isValid();
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble minOf(long a, Element<?> b) {
		double bv = ((ScalarElement)b).doubleValue();
		if (a < bv)
			value = (double)a;
		else
			value = (double)bv;
		valid = b.isValid();
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble minOf(Element<?> a, double b) {
		double av = ((ScalarElement)a).doubleValue();
		if (av < b)
			value = (double)av;
		else
			value = (double)b;
		valid = a.isValid();
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble minOf(double a, Element<?> b) {
		double bv = ((ScalarElement)b).doubleValue();
		if (a < bv)
			value = (double)a;
		else
			value = (double)bv;
		valid = b.isValid();
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble minOf(Element<?> a, Element<?> b) {
		double av = ((ScalarElement)a).doubleValue();
		double bv = ((ScalarElement)b).doubleValue();
		if (av < bv)
			value = (double)av;
		else
			value = (double)bv;
		valid = a.isValid() && b.isValid();
		return this;
	}

	@Override
	public ElementDouble minOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			minOf(a, b);
		return this;
	}
	@Override
	public ElementDouble minOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			minOf(a, b);
		return this;
	}

	@Override
	public ElementDouble max(long other) {
		if (other > value)
			value = (double)other;
		return this;
	}
	@Override
	public ElementDouble max(double other) {
		if (other > value)
			value = other;
		return this;
	}
	@Override
	public ElementDouble max(Element<?> other) {
		if (!other.isValid()) {
			this.setValid(false);
			return this;
		}
		if (((ScalarElement)other).doubleValue() > value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}

	@Override
	public ElementDouble maxIfValid(long other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		if (other > value)
			value = (double)other;
		return this;
	}
	@Override
	public ElementDouble maxIfValid(double other, Element<?> mask) {
		if (!mask.isValid())
			return this;
		if (other > value)
			value = other;
		return this;
	}
	@Override
	public ElementDouble maxIfValid(Element<?> other) {
		if (!other.isValid())
			return this;
		if (((ScalarElement)other).doubleValue() > value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}
	@Override
	public ElementDouble maxIfValid(Element<?> other, Element<?> mask) {
		if (!other.isValid() || !mask.isValid())
			return this;
		if (((ScalarElement)other).doubleValue() > value)
			value = ((ScalarElement)other).doubleValue();
		return this;
	}

	@Override
	public ElementDouble maxNew(long other) {
		ElementDouble res = copy();
		return res.max(other);
	}
	@Override
	public ElementDouble maxNew(double other) {
		ElementDouble res = copy();
		return res.max(other);
	}
	@Override
	public ElementDouble maxNew(Element<?> other) {
		ElementDouble res = copy();
		return res.max(other);
	}

	@Override
	public ElementDouble maxNewIfValid(long other, Element<?> mask) {
		ElementDouble res = copy();
		return res.maxIfValid(other, mask);
	}
	@Override
	public ElementDouble maxNewIfValid(double other, Element<?> mask) {
		ElementDouble res = copy();
		return res.maxIfValid(other, mask);
	}
	@Override
	public ElementDouble maxNewIfValid(Element<?> other) {
		ElementDouble res = copy();
		return res.maxIfValid(other);
	}
	@Override
	public ElementDouble maxNewIfValid(Element<?> other, Element<?> mask) {
		ElementDouble res = copy();
		return res.maxIfValid(other, mask);
	}

	@Override
	public ElementDouble maxOf(long a, long b) {
		if (a > b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble maxOf(double a, long b) {
		if (a > b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble maxOf(long a, double b) {
		if (a > b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	@Override
	public ElementDouble maxOf(double a, double b) {
		if (a > b)
			value = (double)a;
		else
			value = (double)b;
		valid = true;
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble maxOf(Element<?> a, long b) {
		double av = ((ScalarElement)a).doubleValue();
		if (av > b)
			value = (double)av;
		else
			value = (double)b;
		valid = a.isValid();
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble maxOf(long a, Element<?> b) {
		double bv = ((ScalarElement)b).doubleValue();
		if (a > bv)
			value = (double)a;
		else
			value = (double)bv;
		valid = b.isValid();
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @throws ClassCastException if a is a vector.
	 */
	@Override
	public ElementDouble maxOf(Element<?> a, double b) {
		double av = ((ScalarElement)a).doubleValue();
		if (av > b)
			value = (double)av;
		else
			value = (double)b;
		valid = a.isValid();
		return this;
	}
	/**
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if b is a vector.
	 */
	@Override
	public ElementDouble maxOf(double a, Element<?> b) {
		double bv = ((ScalarElement)b).doubleValue();
		if (a > bv)
			value = (double)a;
		else
			value = (double)bv;
		valid = b.isValid();
		return this;
	}
	/**
	 * @param a This will be converted to double before the operation.
	 * @param b This will be converted to double before the operation.
	 * @throws ClassCastException if a or b are vectors.
	 */
	@Override
	public ElementDouble maxOf(Element<?> a, Element<?> b) {
		double av = ((ScalarElement)a).doubleValue();
		double bv = ((ScalarElement)b).doubleValue();
		if (av > bv)
			value = (double)av;
		else
			value = (double)bv;
		valid = a.isValid() && b.isValid();
		return this;
	}

	@Override
	public ElementDouble maxOfIfValid(long a, long b, Element<?> mask) {
		if (mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(double a, long b, Element<?> mask) {
		if (mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(long a, double b, Element<?> mask) {
		if (mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(double a, double b, Element<?> mask) {
		if (mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, long b) {
		if (a.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, long b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(long a, Element<?> b) {
		if (b.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(long a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, double b) {
		if (a.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, double b, Element<?> mask) {
		if (a.isValid() && mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(double a, Element<?> b) {
		if (b.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(double a, Element<?> b, Element<?> mask) {
		if (b.isValid() && mask.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, Element<?> b) {
		if (a.isValid() && b.isValid())
			maxOf(a, b);
		return this;
	}
	@Override
	public ElementDouble maxOfIfValid(Element<?> a, Element<?> b, Element<?> mask) {
		if (a.isValid() && b.isValid() && mask.isValid())
			maxOf(a, b);
		return this;
	}

	@Override
	public ElementDouble clamp(long min, long max) {
		if (value < min)
			value = (double)min;
		else if (value > max)
			value = (double)max;
		return this;
	}
	@Override
	public ElementDouble clamp(double min, double max) {
		if (value < min)
			value = min;
		else if (value > max)
			value = max;
		return this;
	}
	@Override
	public ElementDouble clamp(Element<?> min, Element<?> max) {
		if (!min.isValid() || !max.isValid()) {
			this.setValid(false);
			return this;
		}
		// This may throw an exception - but that's OK, because VectorElements
		// are not comparable.
		ScalarElement minS = (ScalarElement)min;
		ScalarElement maxS = (ScalarElement)max;
		if (value < minS.doubleValue())
			value = minS.doubleValue();
		else if (value > maxS.doubleValue())
			value = maxS.doubleValue();
		return this;
	}

	@Override
	public ElementDouble clampNew(long min, long max) {
		ElementDouble res = copy();
		return res.clamp(min, max);
	}
	@Override
	public ElementDouble clampNew(double min, double max) {
		ElementDouble res = copy();
		return res.clamp(min, max);
	}
	@Override
	public ElementDouble clampNew(Element<?> min, Element<?> max) {
		ElementDouble res = copy();
		return res.clamp(min, max);
	}

	@Override
	public int compareTo(long other) {
		if (value > other)
			return 1;
		else if (other == value)
			return 0;
		else
			return -1;
	}
	@Override
	public int compareTo(double other) {
		if (value > other)
			return 1;
		else if (other == value)
			return 0;
		else
			return -1;
	}
	@Override
	public int compareTo(ScalarElement other) {
		if (ElementByte.class == other.getClass()) {
			if (value > other.byteValue())
				return 1;
			else if (value == other.byteValue())
				return 0;
			else
				return -1;

		} else if (ElementShort.class == other.getClass()) {
			if (value > other.shortValue())
				return 1;
			else if (value == other.shortValue())
				return 0;
			else
				return -1;

		} else if (ElementInt.class == other.getClass()) {
			if (value > other.intValue())
				return 1;
			else if (value == other.intValue())
				return 0;
			else
				return -1;

		} else if (ElementLong.class == other.getClass()) {
			if (value > other.longValue())
				return 1;
			else if (value == other.longValue())
				return 0;
			else
				return -1;

		} else if (ElementFloat.class == other.getClass()) {
			if (value > other.floatValue())
				return 1;
			else if (value == other.floatValue())
				return 0;
			else
				return -1;

		} else if (ElementDouble.class == other.getClass()) {
			if (value > other.doubleValue())
				return 1;
			else if (value == other.doubleValue())
				return 0;
			else
				return -1;

		} else {
			throw new UnsupportedOperationException(String.format(
					"No known comparison for type %s", other.getClass()));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (!ScalarElement.class.isAssignableFrom(obj.getClass()))
			return false;

		ScalarElement other = (ScalarElement) obj;
		if (valid != other.isValid())
			return false;

		if (ElementByte.class == obj.getClass()) {
			return value == other.byteValue();
		} else if (ElementShort.class == obj.getClass()) {
			return value == other.shortValue();
		} else if (ElementInt.class == obj.getClass()) {
			return value == other.intValue();
		} else if (ElementLong.class == obj.getClass()) {
			return value == other.longValue();
		} else if (ElementFloat.class == obj.getClass()) {
			return value == other.floatValue();
		} else if (ElementDouble.class == obj.getClass()) {
			return value == other.doubleValue();
		} else {
			throw new UnsupportedOperationException(String.format(
					"No known comparison for type %s", other.getClass()));
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		long temp;
		temp = Double.doubleToLongBits(value);
		return prime + (int) (temp ^ (temp >>> 32));
	}


	@Override
	public String toString() {
		if (!isValid())
			return String.format("!%g", value);
		else
			return String.format("%g", value);
	}
}
