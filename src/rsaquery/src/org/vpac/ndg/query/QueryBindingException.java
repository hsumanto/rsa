package org.vpac.ndg.query;

/**
 * Indicates that a filter has been misconfigured, probably due to a faulty
 * query definition.
 * @author Alex Fraser
 */
public class QueryBindingException extends QueryException {
	private static final long serialVersionUID = -2328755998611259276L;

	public QueryBindingException() {
		super();
	}

	public QueryBindingException(String s) {
		super(s);
	}

	public QueryBindingException(Throwable e) {
		super(e);
	}

	public QueryBindingException(String s, Throwable e) {
		super(s, e);
	}
}