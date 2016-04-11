package org.vpac.ndg.query;

import java.lang.reflect.Field;

public class FilterDebug {

	private FilterAdapter context;

	public FilterDebug(FilterAdapter context) {
		this.context = context;
	}

	/**
	 * @param fieldName The name of the field.
	 * @return The name of the member, qualified by the name of the inner filter
	 *         (e.g. Foo.bar). This should be used for messages that relate to
	 *         the <em>class</em>, e.g. bugs, rather than the instance.
	 * @see #pathStr(String)
	 */
	public String memberStr(String fieldName){
		String className = context.getInnerFilter().getClass().getSimpleName();
		return String.format("%s.%s", className, fieldName);
	}

	/**
	 * @see #pathStr(String)
	 */
	public Object memberStr(Field field) {
		String className = field.getDeclaringClass().getSimpleName();
		return String.format("%s.%s", className, field.getName());
	}

	/**
	 * @param fieldName The name of the field.
	 * @return The path of the member, qualified by the ID of the node it is
	 *         being used in (e.g. #foo/bar). (e.g. Foo.bar). This should be
	 *         used for messages that relate to the <em>instance</em>, e.g.
	 *         configuration errors, rather than the class.
	 * @see #memberStr(String)
	 */
	public String pathStr(String fieldName){
		return String.format("#%s/%s", context.getName(), fieldName);
	}

	/**
	 * @see #pathStr(String)
	 */
	public String pathStr(Field field) {
		return pathStr(field.getName());
	}
}
