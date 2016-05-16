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

package org.vpac.ndg.storage.dao;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.ParameterizedType;

/**
 * SQL ARRAY mapper for arrays of boxed primitive types
 * @author Alex Fraser
 */
public class ArrayType implements CompositeUserType, ParameterizedType {

	private String type;
	private String sqlType;

	@Override
	public void setParameterValues(Properties parameters) {
		if (parameters == null)
			type = "Double";
		else
			type = parameters.getProperty("type", "Double");

		if (type.equals("Float"))
			sqlType = "real";
		else if (type.equals("Double"))
			sqlType = "double precision";
		else if (type.equals("Short"))
			sqlType = "smallint";
		else if (type.equals("Integer"))
			sqlType = "integer";
		else if (type.equals("Long"))
			sqlType = "bigint";
		else if (type.equals("String"))
			sqlType = "text";
		else {
			throw new IllegalArgumentException(
				String.format("Unsupported type %s", type));
		}
	}

	@Override
	public String[] getPropertyNames() {
		return new String[] {};
	}

	@Override
	public Type[] getPropertyTypes() {
		return new Type[] {};
	}

	@Override
	public void setPropertyValue(Object component, int property, Object value)
			throws HibernateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getPropertyValue(Object component, int property)
			throws HibernateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> returnedClass() {
		return String[].class;
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
		return resultSet.getArray(names[0]).getArray();
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value,
			int index, SessionImplementor session)
			throws HibernateException, SQLException {

		if (value == null) {
			statement.setNull(index, Types.ARRAY);
			return;
		}

		Array array = session.connection().createArrayOf(
			sqlType, (Object[]) value);
		statement.setArray(index, array);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return Arrays.copyOf((Object[]) value, ((Object[]) value).length);
	}

	@Override
	public Object assemble(Serializable cached, SessionImplementor session,
			Object owner) throws HibernateException {
		return Arrays.copyOf((Object[]) cached, ((Object[]) cached).length);
	}

	@Override
	public Serializable disassemble(Object value, SessionImplementor session)
			throws HibernateException {
		return Arrays.copyOf((Object[]) value, ((Object[]) value).length);
	}

	@Override
	public Object replace(Object original, Object target,
			SessionImplementor session, Object owner)
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
			return Arrays.equals((Object[]) a, (Object[]) b);
		}
	}

	@Override
	public int hashCode(Object value) throws HibernateException {
		return Arrays.hashCode((Object[]) value);
	}

}
