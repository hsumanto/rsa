package org.vpac.ndg.storage;

/**
 * java.sql.Types doesn't define some types supported by Postgres. For example,
 * it defines ARRAY but Postgres uses typed arrays.
 *
 * @author Alex Fraser
 */
public class CustomTypes {
	public final static int FLOAT_ARRAY = 8000;
	public final static int DOUBLE_ARRAY = 8001;
	public final static int SMALLINT_ARRAY = 8002;
	public final static int INTEGER_ARRAY = 8003;
	public final static int BIGINT_ARRAY = 8004;
	public final static int LONGVARCHAR_ARRAY = 8005;
}
