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

// THIS IS GENERATED CODE. Do not modify this file. See Swizzle_gen.py.

package org.vpac.ndg.query.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transfers values from one vector to another, rearranging the components along
 * the way.
 *
 * <pre>
 *     VectorLong source = VectorLong.create(1, 2);
 *     VectorLong target = VectorLong.createEmpty(3); // target == (0, 0, 0)
 *     Swizzle swiz = SwizzleFactory.compile("xxy");
 *     swiz.swizzle(source, target); // target == (2, 2, 1)
 * </pre>
 * @author Alex Fraser
 */
public abstract class Swizzle {

    /**
	 * Reorganise the components of a vec	
	 *
	 * @param in The vector to use as the source.
	 * @param out The vector to write to. It must have the same number of
	 *        components as was specified when creating this swizzle instance.
	 */
	public abstract void swizzle(VectorInt in, VectorInt out);

	/**
	 * Reorganise the components of a vector.
	 *
	 * @param in The vector to use as the source.
	 * @param out The vector to write to. It must have the same number of
	 *        components as was specified when creating this swizzle instance.
	 */
	public abstract void swizzle(VectorReal in, VectorReal out);

	/**
	 * Reorganise the components of a vector. The type of each component will
	 * not be changed; the source values (in) will be converted into to the
	 * types of the target (out).
	 *
	 * @param in The vector to use as the source.
	 * @param out The vector to write to. It must have the same number of
	 *        components as was specified when creating this swizzle instance.
	 */
	public abstract void swizzle(VectorElement in, VectorElement out);

	/**
	 * Reorganise the components of a box.
	 *
	 * @param in The box to use as the source.
	 * @param out The box to write to. It must have the same number of
	 *        dimensions as was specified when creating this swizzle instance.
	 */
	public void swizzle(BoxInt in, BoxInt out) {
		swizzle(in.getMin(), out.getMin());
		swizzle(in.getMax(), out.getMax());
	}

	/**
	 * Reorganise the components of a box.
	 *
	 * @param in The box to use as the source.
	 * @param out The box to write to. It must have the same number of
	 *        dimensions as was specified when creating this swizzle instance.
	 */
	public void swizzle(BoxReal in, BoxReal out) {
		swizzle(in.getMin(), out.getMin());
		swizzle(in.getMax(), out.getMax());
	}

	/**
	 * Reorganise the components of an array.
	 *
	 * @param in The array to use as the source.
	 * @param out The array to write to. It must have the same number of
	 *            components as was specified when creating this swizzle
	 *            instance.
	 */
	public abstract <T> void swizzle(T[] in, T[] out);

	/**
	 * Create a new swizzle that does the opposite of this one: from and to are
	 * reversed.
	 * 
	 * @return The new swizzle object.
	 */
	public abstract Swizzle invert();


	/**
	 * Abstracts component access from a vector.
	 * @author Alex Fraser
	 */
	interface SwizzleOp {
		public long get(VectorInt from);
		public double get(VectorReal from);
		public ScalarElement get(VectorElement from);
		public Object get(Object[] from);

		public void set(VectorInt to, long value);
		public void set(VectorReal to, double value);
		public void set(VectorElement to, ScalarElement value);
		public void set(Object[] to, Object value);
	}

	static class SwizzleOp0 implements SwizzleOp {

		ScalarElement prototype = new ElementInt(0);

		@Override
		public long get(VectorInt from) {
			return 0;
		}

		@Override
		public double get(VectorReal from) {
			return 0.0;
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return prototype;
		}

		@Override
		public Object get(Object[] from) {
			return null;
		}

		@Override
		public String toString() {
			return "0";
		}

		@Override
		public void set(VectorInt to, long value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(VectorReal to, double value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(VectorElement to, ScalarElement value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(Object[] to, Object value) {
			// Nothing to do for this virtual component.
		}
	}

	static class SwizzleOp1 implements SwizzleOp {

		ScalarElement prototype = new ElementInt(1);

		@Override
		public long get(VectorInt from) {
			return 1;
		}

		@Override
		public double get(VectorReal from) {
			return 1.0;
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return prototype;
		}

		@Override
		public Object get(Object[] from) {
			// There is "1" Object, so behave like "0" (null)   
			return null;
		}

		@Override
		public String toString() {
			return "1";
		}

		@Override
		public void set(VectorInt to, long value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(VectorReal to, double value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(VectorElement to, ScalarElement value) {
			// Nothing to do for this virtual component.
		}

		@Override
		public void set(Object[] to, Object value) {
			// Nothing to do for this virtual component.
		}
	}


	static SwizzleOp getOperation(char axis) {
		switch (axis) {
		case '0':
			return new SwizzleOp0();
		case '1':
			return new SwizzleOp1();

		case 'x':
			return new SwizzleOpX();

		case 'y':
			return new SwizzleOpY();

		case 'z':
			return new SwizzleOpZ();

		case 'w':
			return new SwizzleOpW();

		case 'a':
			return new SwizzleOpA();

		case 'b':
			return new SwizzleOpB();

		case 'c':
			return new SwizzleOpC();

		case 'd':
			return new SwizzleOpD();

		case 'e':
			return new SwizzleOpE();

		case 'f':
			return new SwizzleOpF();

		case 'g':
			return new SwizzleOpG();

		case 'h':
			return new SwizzleOpH();

		case 'i':
			return new SwizzleOpI();

		case 'j':
			return new SwizzleOpJ();

		case 't':
			return new SwizzleOpT();

		default:
			throw new IllegalArgumentException(String.format(
					"Invalid swizzle axis specified: \"%c\".", axis));
		}
	}

	static SwizzleOp getOperation(String dimension) {
		if (dimension.equals("0") || dimension.equals("zero"))
			return new SwizzleOp0();
		else if (dimension.equals("1") || dimension.equals("one"))
			return new SwizzleOp1();
		else if (dimension.equals("t") || dimension.equals("time"))
			return new SwizzleOpT();

		else if (dimension.equals("x"))
			return new SwizzleOpX();

		else if (dimension.equals("y"))
			return new SwizzleOpY();

		else if (dimension.equals("z"))
			return new SwizzleOpZ();

		else if (dimension.equals("w"))
			return new SwizzleOpW();

		else if (dimension.equals("a"))
			return new SwizzleOpA();

		else if (dimension.equals("b"))
			return new SwizzleOpB();

		else if (dimension.equals("c"))
			return new SwizzleOpC();

		else if (dimension.equals("d"))
			return new SwizzleOpD();

		else if (dimension.equals("e"))
			return new SwizzleOpE();

		else if (dimension.equals("f"))
			return new SwizzleOpF();

		else if (dimension.equals("g"))
			return new SwizzleOpG();

		else if (dimension.equals("h"))
			return new SwizzleOpH();

		else if (dimension.equals("i"))
			return new SwizzleOpI();

		else if (dimension.equals("j"))
			return new SwizzleOpJ();

		else {
			throw new IllegalArgumentException(String.format(
					"Invalid swizzle dimension specified: \"%s\".", dimension));
		}
	}

	static class SwizzleOpX implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getX();
		}

		@Override
		public double get(VectorReal from) {
			return from.getX();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getX();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getX(from.length)];
		}

		@Override
		public String toString() {
			return "x";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setX(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setX(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setX(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getX(to.length)] = value;
		}
	}

	static class SwizzleOpY implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getY();
		}

		@Override
		public double get(VectorReal from) {
			return from.getY();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getY();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getY(from.length)];
		}

		@Override
		public String toString() {
			return "y";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setY(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setY(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setY(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getY(to.length)] = value;
		}
	}

	static class SwizzleOpZ implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getZ();
		}

		@Override
		public double get(VectorReal from) {
			return from.getZ();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getZ();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getZ(from.length)];
		}

		@Override
		public String toString() {
			return "z";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setZ(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setZ(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setZ(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getZ(to.length)] = value;
		}
	}

	static class SwizzleOpW implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getW();
		}

		@Override
		public double get(VectorReal from) {
			return from.getW();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getW();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getW(from.length)];
		}

		@Override
		public String toString() {
			return "w";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setW(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setW(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setW(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getW(to.length)] = value;
		}
	}

	static class SwizzleOpA implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getA();
		}

		@Override
		public double get(VectorReal from) {
			return from.getA();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getA();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getA(from.length)];
		}

		@Override
		public String toString() {
			return "a";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setA(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setA(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setA(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getA(to.length)] = value;
		}
	}

	static class SwizzleOpB implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getB();
		}

		@Override
		public double get(VectorReal from) {
			return from.getB();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getB();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getB(from.length)];
		}

		@Override
		public String toString() {
			return "b";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setB(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setB(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setB(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getB(to.length)] = value;
		}
	}

	static class SwizzleOpC implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getC();
		}

		@Override
		public double get(VectorReal from) {
			return from.getC();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getC();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getC(from.length)];
		}

		@Override
		public String toString() {
			return "c";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setC(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setC(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setC(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getC(to.length)] = value;
		}
	}

	static class SwizzleOpD implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getD();
		}

		@Override
		public double get(VectorReal from) {
			return from.getD();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getD();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getD(from.length)];
		}

		@Override
		public String toString() {
			return "d";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setD(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setD(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setD(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getD(to.length)] = value;
		}
	}

	static class SwizzleOpE implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getE();
		}

		@Override
		public double get(VectorReal from) {
			return from.getE();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getE();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getE(from.length)];
		}

		@Override
		public String toString() {
			return "e";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setE(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setE(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setE(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getE(to.length)] = value;
		}
	}

	static class SwizzleOpF implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getF();
		}

		@Override
		public double get(VectorReal from) {
			return from.getF();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getF();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getF(from.length)];
		}

		@Override
		public String toString() {
			return "f";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setF(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setF(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setF(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getF(to.length)] = value;
		}
	}

	static class SwizzleOpG implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getG();
		}

		@Override
		public double get(VectorReal from) {
			return from.getG();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getG();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getG(from.length)];
		}

		@Override
		public String toString() {
			return "g";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setG(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setG(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setG(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getG(to.length)] = value;
		}
	}

	static class SwizzleOpH implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getH();
		}

		@Override
		public double get(VectorReal from) {
			return from.getH();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getH();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getH(from.length)];
		}

		@Override
		public String toString() {
			return "h";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setH(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setH(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setH(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getH(to.length)] = value;
		}
	}

	static class SwizzleOpI implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getI();
		}

		@Override
		public double get(VectorReal from) {
			return from.getI();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getI();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getI(from.length)];
		}

		@Override
		public String toString() {
			return "i";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setI(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setI(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setI(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getI(to.length)] = value;
		}
	}

	static class SwizzleOpJ implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getJ();
		}

		@Override
		public double get(VectorReal from) {
			return from.getJ();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getJ();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getJ(from.length)];
		}

		@Override
		public String toString() {
			return "j";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setJ(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setJ(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setJ(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getJ(to.length)] = value;
		}
	}

	static class SwizzleOpT implements SwizzleOp {

		@Override
		public long get(VectorInt from) {
			return from.getT();
		}

		@Override
		public double get(VectorReal from) {
			return from.getT();
		}

		@Override
		public ScalarElement get(VectorElement from) {
			return from.getT();
		}

		@Override
		public Object get(Object[] from) {
			return from[ComponentLUT.getT(from.length)];
		}

		@Override
		public String toString() {
			return "t";
		}

		@Override
		public void set(VectorInt to, long value) {
			to.setT(value);
		}

		@Override
		public void set(VectorReal to, double value) {
			to.setT(value);
		}

		@Override
		public void set(VectorElement from, ScalarElement value) {
			from.setT(value);
		}

		@Override
		public void set(Object[] to, Object value) {
			to[ComponentLUT.getT(to.length)] = value;
		}
	}

	static class Swizzle0 extends Swizzle {


		public Swizzle0(List<SwizzleOp> from, List<SwizzleOp> to) {

		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

		}

		@Override
		public Swizzle0 invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			return new Swizzle0(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

	static class Swizzle1 extends Swizzle {

		SwizzleOp from0;
		SwizzleOp to0;

		public Swizzle1(List<SwizzleOp> from, List<SwizzleOp> to) {

			from0 = from.get(0);
			to0 = to.get(0);
		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

			to0.set(out, from0.get(in));
		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

			to0.set(out, from0.get(in));
		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

			to0.set(out, from0.get(in));
		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

			to0.set(out, from0.get(in));
		}

		@Override
		public Swizzle1 invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			to.add(to0);
			from.add(from0);
			return new Swizzle1(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			fromstr += from0;
			tostr += to0;
			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

	static class Swizzle2 extends Swizzle {

		SwizzleOp from0;
		SwizzleOp to0;
		SwizzleOp from1;
		SwizzleOp to1;

		public Swizzle2(List<SwizzleOp> from, List<SwizzleOp> to) {

			from0 = from.get(0);
			to0 = to.get(0);
			from1 = from.get(1);
			to1 = to.get(1);
		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
		}

		@Override
		public Swizzle2 invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			to.add(to0);
			from.add(from0);
			to.add(to1);
			from.add(from1);
			return new Swizzle2(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			fromstr += from0;
			tostr += to0;
			fromstr += from1;
			tostr += to1;
			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

	static class Swizzle3 extends Swizzle {

		SwizzleOp from0;
		SwizzleOp to0;
		SwizzleOp from1;
		SwizzleOp to1;
		SwizzleOp from2;
		SwizzleOp to2;

		public Swizzle3(List<SwizzleOp> from, List<SwizzleOp> to) {

			from0 = from.get(0);
			to0 = to.get(0);
			from1 = from.get(1);
			to1 = to.get(1);
			from2 = from.get(2);
			to2 = to.get(2);
		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
		}

		@Override
		public Swizzle3 invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			to.add(to0);
			from.add(from0);
			to.add(to1);
			from.add(from1);
			to.add(to2);
			from.add(from2);
			return new Swizzle3(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			fromstr += from0;
			tostr += to0;
			fromstr += from1;
			tostr += to1;
			fromstr += from2;
			tostr += to2;
			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

	static class Swizzle4 extends Swizzle {

		SwizzleOp from0;
		SwizzleOp to0;
		SwizzleOp from1;
		SwizzleOp to1;
		SwizzleOp from2;
		SwizzleOp to2;
		SwizzleOp from3;
		SwizzleOp to3;

		public Swizzle4(List<SwizzleOp> from, List<SwizzleOp> to) {

			from0 = from.get(0);
			to0 = to.get(0);
			from1 = from.get(1);
			to1 = to.get(1);
			from2 = from.get(2);
			to2 = to.get(2);
			from3 = from.get(3);
			to3 = to.get(3);
		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
			to3.set(out, from3.get(in));
		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
			to3.set(out, from3.get(in));
		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
			to3.set(out, from3.get(in));
		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

			to0.set(out, from0.get(in));
			to1.set(out, from1.get(in));
			to2.set(out, from2.get(in));
			to3.set(out, from3.get(in));
		}

		@Override
		public Swizzle4 invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			to.add(to0);
			from.add(from0);
			to.add(to1);
			from.add(from1);
			to.add(to2);
			from.add(from2);
			to.add(to3);
			from.add(from3);
			return new Swizzle4(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			fromstr += from0;
			tostr += to0;
			fromstr += from1;
			tostr += to1;
			fromstr += from2;
			tostr += to2;
			fromstr += from3;
			tostr += to3;
			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

	static class SwizzleN extends Swizzle {

		SwizzleOp[] from;
		SwizzleOp[] to;

		public SwizzleN(List<SwizzleOp> from, List<SwizzleOp> to) {

			this.from = new SwizzleOp[from.size()];
			this.to = new SwizzleOp[to.size()];
			for (int i = 0; i < from.size(); i++) {
				this.from[i] = from.get(i);
				this.to[i] = to.get(i);
			}
		}

		@Override
		public void swizzle(VectorInt in, VectorInt out) {

			for (int i = 0; i < from.length; i++) {
				to[i].set(out, from[i].get(in));
			}
		}

		@Override
		public void swizzle(VectorReal in, VectorReal out) {

			for (int i = 0; i < from.length; i++) {
				to[i].set(out, from[i].get(in));
			}
		}

		@Override
		public void swizzle(VectorElement in, VectorElement out) {

			for (int i = 0; i < from.length; i++) {
				to[i].set(out, from[i].get(in));
			}
		}

		@Override
		public <T> void swizzle(T[] in, T[] out) {

			for (int i = 0; i < from.length; i++) {
				to[i].set(out, from[i].get(in));
			}
		}

		@Override
		public SwizzleN invert() {
			List<SwizzleOp> from = new ArrayList<SwizzleOp>();
			List<SwizzleOp> to = new ArrayList<SwizzleOp>();

			to.addAll(Arrays.asList(this.to));
			from.addAll(Arrays.asList(this.from));
			return new SwizzleN(to, from);
		}

		@Override
		public String toString() {
			String fromstr = "";
			String tostr = "";

			for (int i = 0; i < from.length; i++) {
				fromstr += from[i];
				tostr += to[i];
			}
			return String.format("Swizzle(%s -> %s)", fromstr, tostr);
		}
	}

}
