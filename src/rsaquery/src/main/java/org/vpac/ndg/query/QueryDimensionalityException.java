package org.vpac.ndg.query;

public class QueryDimensionalityException extends QueryBindingException {

	private static final long serialVersionUID = -8677937336959953134L;

	public QueryDimensionalityException() {
	}

	public QueryDimensionalityException(String s) {
		super(s);
	}

	public QueryDimensionalityException(Throwable e) {
		super(e);
	}

	public QueryDimensionalityException(String s, Throwable e) {
		super(s, e);
	}

}
