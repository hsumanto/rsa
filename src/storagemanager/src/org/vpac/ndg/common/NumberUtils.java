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
        x = x % 1.0;
        long multiplier = (1L << (digits * 4));
        long fraction = (long)(x * multiplier);

        // Convert integer to hex string.
        // String should have at least n digits; prefix with zeros if not.
        String hex = Long.toHexString(fraction);
        String padding = new String(new char[digits]).replace("\0", "0");
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
        return Long.toHexString(whole);
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

    public static double lerp(double a, double b, double fraction) {
        return ((b - a) * fraction) + a;
    }

    public static double unlerp(double a, double b, double value) {
        double divisor = b - a;
        if (divisor == 0)
            return 0;
        return (value - a) / divisor;
    }
}
