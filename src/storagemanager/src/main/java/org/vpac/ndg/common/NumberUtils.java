package org.vpac.ndg.common;

/**
 * Utilities for working with numbers.
 * @author Alex Fraser
 */
public class NumberUtils {

	/**
	 * Render the fractional part of a real number as a string of hexadecimal
	 * digits.
	 * @param x The number to render.
	 * @param digits The number of digits in the answer.
	 * @return A hexadecimal string.
	 */
    public static String toHexFraction(double x, int digits) {
        // Get fractional part, and shift left by n digits.
        if (x < 0.0)
            x = 0.0 - x;
        x = x % 1.0;
        long multiplier = (1L << (digits * 4));
        long fraction = (long)(x * multiplier);

        // Convert integer to hex string.
        // String should have at least n digits; prefix with zeros if not.
        String hex = Long.toHexString(fraction);
        String padding = "000000000000000";
        hex = padding.substring(0, digits - hex.length()) + hex;

        return hex;
    }

    /**
     * Render the integer part of a real number as a string of hexadecimal
     * digits.
     * @param x The number to render.
     * @return A hexadecimal string.
     */
    public static String toHexInteger(double x) {
        long whole = (long) x;
        String prefix;
        if (whole < 0) {
            // Long.toHexString treats the number as an unsigned integer.
            whole = 0 - whole;
            prefix = "-";
        } else {
            prefix = "";
        }
        return prefix + Long.toHexString(whole);
    }

    /**
     * Render a real number as a string of hexadecimal digits, like
     * JavaScript's Number.toString(16).
     * @param x The number to render.
     * @param digits The number of digits to return after the decimal point.
     * @return A hexadecimal string of the form INT.FRACTION, e.g. f8.1e93b9.
     */
    public static String toHex(double x, int digits) {
        return toHexInteger(x) + "." + toHexFraction(x, digits);
    }

    /**
	 * Linearly interpolate between two values.
	 *
	 * @param a The first value.
	 * @param b The second value.
	 * @param fraction The point to interpolate to, with 0 being a and 1 being
	 *            b.
	 * @return A value between a and b (if fraction is between 0 and 1).
	 */
    public static double lerp(double a, double b, double fraction) {
        return ((b - a) * fraction) + a;
    }

    /**
	 * Inverse linear interpolation between two values. Formally:
	 *
	 * <pre>
	 * lerp(a, b, unlerp(a, b, x)) == x
	 * </pre>
	 *
	 * <p>
	 * e.g. unlerp(1, 3, 2) == 0.5
	 * </p>
	 *
	 * <p>
	 * If a == b, the result is always 0.
	 * </p>
	 *
	 * @param a The lower bound.
	 * @param b The upper bound.
	 * @param value The value to find the interpolation point of.
	 * @return A fraction representing how far value is away from a in the
	 *         direction of b.
	 */
    public static double unlerp(double a, double b, double value) {
        double divisor = b - a;
        if (divisor == 0)
            return 0;
        return (value - a) / divisor;
    }
}
