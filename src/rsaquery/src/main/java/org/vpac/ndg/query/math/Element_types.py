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

from collections import namedtuple

ARITHMETIC_OPS = [
		("add", "+", "add", "to"),
		("sub", "-", "subtract", "from"),
		("mul", "*", "multiply", "with"),
		("div", "/", "divide", "by"),
		("mod", "%", "modulo", "by"),
		]

BOUNDING_OPS = [
		("min", "<", "minimum"),
		("max", ">", "maximum"),
		]

T = namedtuple("T", "formal_name primitive_name boxed_name format_spec special_functions minifier maxifier")
TYPES = [
		T("ElementByte", "byte", "Byte", "%d", """
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + value;
	}
""", "Byte.MIN_VALUE", "Byte.MAX_VALUE"),

		T("ElementShort", "short", "Short", "%d", """
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + value;
	}
""", "Short.MIN_VALUE", "Short.MAX_VALUE"),

		T("ElementInt", "int", "Integer", "%d", """
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + value;
	}
""", "Integer.MIN_VALUE", "Integer.MAX_VALUE"),

		T("ElementLong", "long", "Long", "%d", """
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + (int) (value ^ (value >>> 32));
	}
""", "Long.MIN_VALUE", "Long.MAX_VALUE"),

		T("ElementFloat", "float", "Float", "%g", """
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + Float.floatToIntBits(value);
	}
""", "Float.NEGATIVE_INFINITY", "Float.POSITIVE_INFINITY"),

		T("ElementDouble", "double", "Double", "%g", """
	@Override
	public int hashCode() {
		final int prime = 31;
		long temp;
		temp = Double.doubleToLongBits(value);
		return prime + (int) (temp ^ (temp >>> 32));
	}
""", "Double.NEGATIVE_INFINITY", "Double.POSITIVE_INFINITY"),
		]


ELEMENT_NAMES = [
		("x", "last", "a"),
		("y", "second last", "b"),
		("z", "third last", "c"),
		("w", "fourth last", "d"),

		("a", "last", "x"),
		("b", "second last", "y"),
		("c", "third last", "z"),
		("d", "fourth last", "w"),
		("e", "fifth last", None),
		("f", "sixth last", None),
		("g", "seventh last", None),
		("h", "eighth last", None),
		("i", "ninth last", None),
		("j", "tenth last", None),

		("t", "first", None),

		]
