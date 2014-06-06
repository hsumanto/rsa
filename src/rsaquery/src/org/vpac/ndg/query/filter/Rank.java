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

package org.vpac.ndg.query.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constrain the rank of a public field. If this constraint is not satisfied,an
 * exception will be thrown before the query runs.
 *
 * @author Alex Fraser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Rank {
	/**
	 * @return The name of this group. Grouped fields must have the same number
	 *         of dimensions. If the empty string is used, then the name is the
	 *         same as the field name.
	 */
	String group() default "";

	/**
	 * @return Whether the rank of this field can be increased to match others
	 *         in the group.
	 */
	boolean promote() default false;

	/**
	 * @return Whether the rank of this field can be reduced to match others in
	 *         the group.
	 */
	boolean demote() default false;

	/**
	 * @return The field must have exactly this rank. Set to -1 to disable this
	 *         check.
	 */
	int is() default -1;

	/**
	 * @return The rank of the field must be greater than or equal to this. Set
	 *         to -1 to disable this check.
	 */
	int lowerBound() default -1;

	/**
	 * @return The rank of the field must be less than or equal to this. Set to
	 *         -1 to disable this check.
	 */
	int upperBound() default -1;
}
