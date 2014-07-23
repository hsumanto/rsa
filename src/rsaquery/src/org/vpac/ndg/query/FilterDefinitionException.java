package org.vpac.ndg.query;

/**
 * Indicates that a filter has an error in its programming.
 * @author Alex Fraser
 */
public class FilterDefinitionException extends QueryException {
	private static final long serialVersionUID = -2328755998611259276L;

	public FilterDefinitionException() {
		super();
	}

	public FilterDefinitionException(String s) {
		super(s);
	}

	public FilterDefinitionException(Throwable e) {
		super(e);
	}

	public FilterDefinitionException(String s, Throwable e) {
		super(s, e);
	}
}