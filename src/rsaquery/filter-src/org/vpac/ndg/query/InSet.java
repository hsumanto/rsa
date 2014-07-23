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
 * Copyright 2014 VPAC Innovations Pty Ltd
 * http://vpac-innovations.com.au
 */

package org.vpac.ndg.query;

import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.math.BoxReal;

@Description(name = "In Set", description = "Generates a mask of pixels that are contained in a set.")
@InheritDimensions(from = "input")
public class InSet extends In {

	// Comma-separated list of IDs.
	public String ids;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {
		String[] keys = ids.split(",");
		setBounds(keys, keys);
	}

}