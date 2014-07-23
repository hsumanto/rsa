package org.vpac.ndg.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

	/**
	 * Sets a single field to a value, after converting the value to the
	 * appropriate type. Uses either direct field access or a setter if one is
	 * available. The setter is tried first.
	 *
	 * @param owner The object that owns the field.
	 * @param name The name of the field.
	 * @param value The value to set it to.
	 * @throws QueryException If the field could not be accessed,
	 *             or if the value could not be converted.
	 */
	public static void setSimpleField(Object owner, String name, String value)
			throws QueryException {

		try {
			String setterName = String.format("set%s%s",
					name.substring(0, 1).toUpperCase(), name.substring(1));
			Method method = null;
			for (Method m : owner.getClass().getMethods()) {
				if (!m.getName().equals(setterName))
					continue;
				if (m.getParameterTypes().length != 1)
					continue;
				method = m;
				break;
			}
			if (method == null) {
				throw new NoSuchMethodException(String.format(
						"Could not find method %s with one parameter.",
						setterName));
			}
			Class<?> type = method.getParameterTypes()[0];
			Object parsedValue = parse(owner, name, value, type);
			method.invoke(owner, parsedValue);
			return;

		} catch (ReflectiveOperationException e) {
			// Ignore (try direct field access instead).
		}

		try {
			Field field = owner.getClass().getField(name);
			Class<?> type = field.getType();
			Object parsedValue = parse(owner, name, value, type);
			field.set(owner, parsedValue);
			return;
		} catch (ReflectiveOperationException e) {
			throw new QueryBindingException(String.format(
					"Field %s.%s can not be assigned, and setter could not"
							+ " be called.", owner.getClass().getName(), name), e);
		}
	}

	private static Object parse(Object owner, String name, String value,
			Class<?> type) throws QueryException {
		Object parsedValue = null;
		try {
			if (type == Boolean.TYPE || type.isAssignableFrom(Boolean.class))
				parsedValue = Boolean.parseBoolean(value);
			else if (type == Byte.TYPE || type.isAssignableFrom(Byte.class))
				parsedValue = Byte.parseByte(value);
			else if (type == Short.TYPE || type.isAssignableFrom(Short.class))
				parsedValue = Short.parseShort(value);
			else if (type == Integer.TYPE || type.isAssignableFrom(Integer.class))
				parsedValue = Integer.parseInt(value);
			else if (type == Long.TYPE || type.isAssignableFrom(Long.class))
				parsedValue = Long.parseLong(value);
			else if (type == Float.TYPE || type.isAssignableFrom(Float.class))
				parsedValue = Float.parseFloat(value);
			else if (type == Double.TYPE || type.isAssignableFrom(Double.class))
				parsedValue = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new QueryBindingException(String.format(
					"Type mismatch: field %s.%s should be a %s.",
					owner.getClass().getName(), name, type.getName()), e);
		}

		if (parsedValue == null && type.isAssignableFrom(String.class))
			parsedValue = value;

		if (parsedValue == null) {
			throw new QueryBindingException(String.format(
					"Can't assign field %s.%s: no known conversion to type %s.",
					owner.getClass().getName(), name, type.getName()));
		}
		return parsedValue;
	}

}
