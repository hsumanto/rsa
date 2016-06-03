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
 * Copyright 2016 VPAC Innovations - http://vpac-innovations.com.au
 */

package org.vpac.ndg.storage;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * SQL ARRAY mapper for lists of boxed primitive types, because Hibernate
 * doesn't map arrays by default.
 *
 * @author Alex Fraser
 */
public class EmbeddedListType implements UserType, ParameterizedType {

	private Class<?> componentType;
	private Class<?> clazz;
	private String sqlType;
	private int[] sqlTypeCodes;

	@Override
	public void setParameterValues(Properties parameters) {
		String type;
		if (parameters == null)
			type = "Double";
		else
			type = parameters.getProperty("elementType", "Double");

		// These type strings are specific to Postgres :(
		// http://stackoverflow.com/a/13300045/320036
		if (type.equalsIgnoreCase("Float")) {
			componentType = Float.class;
			sqlType = "float4";
			sqlTypeCodes = new int[] { CustomTypes.FLOAT_ARRAY };
		} else if (type.equalsIgnoreCase("Double")) {
			componentType = Double.class;
			sqlType = "float8";
			sqlTypeCodes = new int[] { CustomTypes.DOUBLE_ARRAY };
		} else if (type.equalsIgnoreCase("Short")) {
			componentType = Short.class;
			sqlType = "int2";
			sqlTypeCodes = new int[] { CustomTypes.SMALLINT_ARRAY };
		} else if (type.equalsIgnoreCase("Integer")) {
			componentType = Integer.class;
			sqlType = "int4";
			sqlTypeCodes = new int[] { CustomTypes.INTEGER_ARRAY };
		} else if (type.equalsIgnoreCase("Long")) {
			componentType = Long.class;
			sqlType = "int8";
			sqlTypeCodes = new int[] { CustomTypes.BIGINT_ARRAY };
		} else if (type.equalsIgnoreCase("String")) {
			componentType = String.class;
			sqlType = "text";
			sqlTypeCodes = new int[] { CustomTypes.LONGVARCHAR_ARRAY };
		} else {
			throw new IllegalArgumentException(
				String.format("Unsupported type %s", type));
		}

		clazz = List.class;
	}

	@Override
	public Class<?> returnedClass() {
		return clazz;
	}

	@Override
	public int[] sqlTypes() {
		return sqlTypeCodes;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names,
			SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		if (resultSet.wasNull())
			return null;
		// Take care here: java.sql.Array#getArray() returns an Object, not
		// Object[] - but java.util.Arrays#asList(Object) takes a varargs array
		// of Object. If a single Object were passed in, it would create a list
		// of Object[] - when in fact we want a list of Object. So the cast to
		// Object[] is required.
		Object[] array = (Object[]) resultSet.getArray(names[0]).getArray();
		return Arrays.asList(array);
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value,
			int index, SessionImplementor session)
			throws HibernateException, SQLException {

		if (value == null) {
			statement.setNull(index, Types.ARRAY);
			return;
		}

		Array array = statement.getConnection().createArrayOf(
			sqlType, ((List) value).toArray());
		statement.setArray(index, array);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		@SuppressWarnings({"unchecked", "rawtypes"})
		List copy = new ArrayList((List) value);
		return copy;
	}

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return Arrays.asList(cached);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return ((List) value).toArray();
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return deepCopy(original);
	}

	@Override
	public boolean equals(Object a, Object b)
			throws HibernateException {
		if (a == b) {
			return true;
		} else if (a == null || b == null) {
			return false;
		} else {
			return a.equals(b);
		}
	}

	@Override
	public int hashCode(Object value) throws HibernateException {
		if (value == null)
			return 0;
		return ((List) value).hashCode();
	}

}
