package org.vpac.ndg.query.filter;


public interface Foldable<F> {

	/**
	 * Combines this object with another.
	 *
	 * This could really be any operation; for example, if this type contains an
	 * integer it could add them together, or if it contains a list they could
	 * be appended.
	 *
	 * @param other
	 *            Another {@link Foldable} of the same type.
	 * @return A new {@link Foldable}, being a combination of this one and the
	 *         other.
	 */
	Foldable<F> fold(F other);

}
