package org.vpac.ndg.query.math;

public class ComponentLUT {

	private final static int OFFSET_X = 0 + 1;
	private final static int OFFSET_Y = 1 + 1;
	private final static int OFFSET_Z = 2 + 1;
	private final static int OFFSET_W = 3 + 1;

	private final static int OFFSET_A = 0 + 1;
	private final static int OFFSET_B = 1 + 1;
	private final static int OFFSET_C = 2 + 1;
	private final static int OFFSET_D = 3 + 1;
	private final static int OFFSET_E = 4 + 1;
	private final static int OFFSET_F = 5 + 1;
	private final static int OFFSET_G = 6 + 1;
	private final static int OFFSET_H = 7 + 1;
	private final static int OFFSET_I = 8 + 1;
	private final static int OFFSET_J = 9 + 1;

	/**
	 * @return The X component (always first).
	 */
	public static int getT(int length) {
		return 0;
	}

	/**
	 * @return The X component (last; synonym for a).
	 */
	public static int getX(int length) {
		return length - OFFSET_X;
	}

	/**
	 * @return The Y component (second last; synonym for b).
	 */
	public static int getY(int length) {
		return length - OFFSET_Y;
	}

	/**
	 * @return The Z component (third last; synonym for c).
	 */
	public static int getZ(int length) {
		return length - OFFSET_Z;
	}

	/**
	 * @return The W component (fourth last; synonym for d).
	 */
	public static int getW(int length) {
		return length - OFFSET_W;
	}

	/**
	 * @return The A component (last; synonym for x).
	 */
	public static int getA(int length) {
		return length - OFFSET_A;
	}

	/**
	 * @return The B component (second last; synonym for y).
	 */
	public static int getB(int length) {
		return length - OFFSET_B;
	}

	/**
	 * @return The C component (third last; synonym for z).
	 */
	public static int getC(int length) {
		return length - OFFSET_C;
	}

	/**
	 * @return The D component (fourth last; synonym for w).
	 */
	public static int getD(int length) {
		return length - OFFSET_D;
	}

	/**
	 * @return The E component (fifth last; synonym for None).
	 */
	public static int getE(int length) {
		return length - OFFSET_E;
	}

	/**
	 * @return The F component (sixth last; synonym for None).
	 */
	public static int getF(int length) {
		return length - OFFSET_F;
	}

	/**
	 * @return The G component (seventh last; synonym for None).
	 */
	public static int getG(int length) {
		return length - OFFSET_G;
	}

	/**
	 * @return The H component (eighth last; synonym for None).
	 */
	public static int getH(int length) {
		return length - OFFSET_H;
	}

	/**
	 * @return The I component (ninth last; synonym for None).
	 */
	public static int getI(int length) {
		return length - OFFSET_I;
	}

	/**
	 * @return The J component (tenth last; synonym for None).
	 */
	public static int getJ(int length) {
		return length - OFFSET_J;
	}

	/**
	 * @return The index of an axis, or -1 if the axis is not present.
	 */
	public static int indexOf(int length, String axis) {
		int i = -1;

		if (axis.equals("time") || axis.equals("t"))
			i = 0;

		else if (axis.equals("x"))
			i = length - OFFSET_X;

		else if (axis.equals("y"))
			i = length - OFFSET_Y;

		else if (axis.equals("z"))
			i = length - OFFSET_Z;

		else if (axis.equals("w"))
			i = length - OFFSET_W;

		else if (axis.equals("a"))
			i = length - OFFSET_A;

		else if (axis.equals("b"))
			i = length - OFFSET_B;

		else if (axis.equals("c"))
			i = length - OFFSET_C;

		else if (axis.equals("d"))
			i = length - OFFSET_D;

		else if (axis.equals("e"))
			i = length - OFFSET_E;

		else if (axis.equals("f"))
			i = length - OFFSET_F;

		else if (axis.equals("g"))
			i = length - OFFSET_G;

		else if (axis.equals("h"))
			i = length - OFFSET_H;

		else if (axis.equals("i"))
			i = length - OFFSET_I;

		else if (axis.equals("j"))
			i = length - OFFSET_J;


		if (i >= length)
			return -1;
		else
			return i;
	}
}
