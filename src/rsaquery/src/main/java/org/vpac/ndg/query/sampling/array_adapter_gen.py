#!/usr/bin/env python

#
# This file is part of the Raster Storage Archive (RSA).
#
# The RSA is free software: you can redistribute it and/or modify it under the
# terms of the GNU General Public License as published by the Free Software
# Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
# A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# the RSA.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
# http://www.crcsi.com.au/
#

#
# This program generates the numeric Element classes.
#

from string import Template


GENERIC_CLASS_HEADER = """/*
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

// THIS FILE IS GENERATED. Do not edit this file. See array_adapter_gen.py.

package org.vpac.ndg.query.sampling;

import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.math.ElementByte;
import org.vpac.ndg.query.math.ElementDouble;
import org.vpac.ndg.query.math.ElementFloat;
import org.vpac.ndg.query.math.ElementInt;
import org.vpac.ndg.query.math.ElementLong;
import org.vpac.ndg.query.math.ElementShort;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.VectorInt;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

public abstract class ArrayAdapterImpl implements ArrayAdapter {

	protected DataType type;
	protected VectorInt shape;
	protected Array array;

	protected ArrayAdapterImpl(DataType type) {
		this.type = type;
		array = null;
		shape = null;
	}

	protected ArrayAdapterImpl(DataType type, Array array) {
		this.type = type;
		this.array = array;
		shape = VectorInt.fromInt(array.getShape());
	}

	/**
	 * Create an array adapter that wraps an existing backing array.
	 * 
	 * @param array The array to wrap.
	 * @param type The data type to use. NOTE: the type may be promoted if the
	 *        array is unsigned.
	 * @param nodataStrategy The strategy to use to identify nodata values.
	 * @return The new adapter.
	 * @throws QueryConfigurationException If the adapter could not be created,
	 *         e.g if the data type has no matching adapter implementation.
	 */
	public static ArrayAdapter createAndPromote(Array array, DataType type,
			NodataStrategy nodataStrategy) throws QueryConfigurationException {

		// Promote type if unsigned.
		Class<? extends ArrayAdapter> aproto = findClass(type, array.isUnsigned());

		try {
			return aproto.getConstructor(Array.class, NodataStrategy.class)
					.newInstance(array, nodataStrategy);

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (QueryConfigurationException.class.isAssignableFrom(e.getCause().getClass()))
				throw (QueryConfigurationException) e;
			else
				throw new QueryConfigurationException(String.format(
						"Could not create array adapter: %s", e.getMessage()),
						e);
		}
	}

	/**
	 * Create a new array adapter with no backing array. The backing array will
	 * be created when resize() is called.
	 * 
	 * @throws QueryConfigurationException If the adapter could not be created,
	 *         e.g if the data type has no matching adapter implementation.
	 */
	public static ArrayAdapter create(DataType type,
			NodataStrategy nodataStrategy) throws QueryConfigurationException {

		Class<? extends ArrayAdapter> aproto = findClass(type, false);

		try {
			return aproto.getConstructor(NodataStrategy.class).newInstance(
					nodataStrategy);

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (QueryConfigurationException.class.isAssignableFrom(e.getCause().getClass()))
				throw (QueryConfigurationException) e;
			else
				throw new QueryConfigurationException(String.format(
						"Could not create array adapter: %s", e.getMessage()),
						e);
		}
	}

	public static Class<? extends ArrayAdapter> findClass(DataType type,
			boolean promote) throws QueryConfigurationException {

		// TODO: Add boolean array adaptor. This will be a special case
		// because arithmetic doesn't make much sense on booleans, so
		// ElementByte should be used instead.
		if (promote) {
			switch(type) {
//			case BOOLEAN:
//				return ArrayAdapterByte.class;
			case BYTE:
				return ArrayAdapterShort.class;
			case SHORT:
				return ArrayAdapterInt.class;
			case INT:
				return ArrayAdapterInt.class;
			case LONG:
				return ArrayAdapterLong.class;
			case FLOAT:
				return ArrayAdapterFloat.class;
			case DOUBLE:
				return ArrayAdapterDouble.class;
			default:
				throw new QueryConfigurationException(String.format(
						"No promoted element type matches %s.", type));
			}
		} else {
			switch (type) {
//			case BOOLEAN:
//				return new ArrayAdapterBoolean(nodataStrategy);
			case BYTE:
				return ArrayAdapterByte.class;
			case SHORT:
				return ArrayAdapterShort.class;
			case INT:
				return ArrayAdapterInt.class;
			case LONG:
				return ArrayAdapterLong.class;
			case FLOAT:
				return ArrayAdapterFloat.class;
			case DOUBLE:
				return ArrayAdapterDouble.class;
			default:
				throw new QueryConfigurationException(String.format(
						"No element type matches %s.", type));
			}
		}
	}

	@Override
	public VectorInt getShape() {
		return shape;
	}

	@Override
	public int getRank() {
		return shape.size();
	}

	@Override
	public void resize(VectorInt shape) {
		if (array != null && this.shape.equals(shape))
			return;
		array = Array.factory(type, shape.asIntArray());
		this.shape = shape.copy();
	}

	@Override
	public Array getArray() {
		return array;
	}

	// SPECIALISED CLASSES

	// A note about performance: benchmark tests and profiling have been run on
	// the getters below. Although ArrayAdapter.get shows up as a hot spot, it
	// is not obvious how to make it faster. A couple of attempts have been made
	// to speed things up:
	//  1. A ScalarElement field was added to ArrayAdapterImpl to use as the
	//     return value for all gets. It was instantiated by the specialised
	//     classes as e.g. ElementByte. This resulted in a 2x speed-up in one
	//     query, but it broke the method contract and resulted in errors.
	//  2. A ScalarElement argument was added to the getters so that they could
	//     be reused. This was functionally correct, but resulted in no speed-up
	//     at all.
	// Option 1 could be made to work if the method contract was changed. Option
	// 2 might be viable if specialised Element types could be passed in to
	// get() - however, this might also require specialisation of SamplerImpl
	// and Page.
"""

GENERIC_CLASS_FOOTER = """
}
"""

SPECIALISED_CLASS = Template("""
	protected static class ArrayAdapter${formalType} extends ArrayAdapterImpl {

		NodataStrategy nodataStrategy;

		public ArrayAdapter${formalType}(NodataStrategy nodataStrategy) {
			super(DataType.${dataType});
			this.nodataStrategy = nodataStrategy;
		}

		public ArrayAdapter${formalType}(Array array,
				NodataStrategy nodataStrategy) {

			super(DataType.${dataType}, array);
			this.nodataStrategy = nodataStrategy;
		}

		@Override
		public ScalarElement get(Index ima) {
			Element${formalType} ret = new Element${formalType}(array.get${formalType}(ima));
			if (nodataStrategy.isNoData(ret))
				ret.setValid(false);
			return ret;
		}

		@Override
		public void set(Index ima, ScalarElement value) {
			if (value.isValid())
				array.set${formalType}(ima, value.${ptype}Value());
			else
				array.set${formalType}(ima, nodataStrategy.getNodataValue().${ptype}Value());
		}

		@Override
		public void unset(Index ima) {
			array.set${formalType}(ima, nodataStrategy.getNodataValue().${ptype}Value());
		}

		@Override
		public ScalarElement get(int i) {
			Element${formalType} ret = new Element${formalType}(array.get${formalType}(i));
			if (nodataStrategy.isNoData(ret))
				ret.setValid(false);
			return ret;
		}

		@Override
		public void set(int i, ScalarElement value) {
			if (value.isValid())
				array.set${formalType}(i, value.${ptype}Value());
			else
				array.set${formalType}(i, nodataStrategy.getNodataValue().${ptype}Value());
		}

		@Override
		public void unset(int i) {
			array.set${formalType}(i, nodataStrategy.getNodataValue().${ptype}Value());
		}

	}
""")

TYPES = [
		("byte"),
		("short"),
		("int"),
		("long"),
		("float"),
		("double"),
		]


# Code

def write_class(output):
	output.write(GENERIC_CLASS_HEADER)

	for ptype in TYPES:
		formalType = ptype.capitalize()
		dataType = ptype.upper()
		mapping = {
				"ptype": ptype,
				"dataType": dataType,
				"formalType": formalType,
				}
		output.write(SPECIALISED_CLASS.substitute(mapping))

	output.write(GENERIC_CLASS_FOOTER)

if __name__ == "__main__":
	with open("ArrayAdapterImpl.java", 'w') as f:
		print "Writing", "ArrayAdapterImpl.java"
		write_class(f)
