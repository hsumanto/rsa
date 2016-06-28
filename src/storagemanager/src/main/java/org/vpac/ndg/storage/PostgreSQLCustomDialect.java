package org.vpac.ndg.storage;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL94Dialect;

/**
 * Hibernate's PostgreSQLDialect doesn't define mappings for the ARRAY type,
 * because there is no possible one-to-one mapping. So we define new types and
 * map those instead.
 *
 * http://stackoverflow.com/questions/37477119/infer-type-information-in-usertype
 *
 * @author Alex Fraser
 */
public class PostgreSQLCustomDialect extends PostgreSQL94Dialect {
    public PostgreSQLCustomDialect() {
        super();
        this.registerColumnType(CustomTypes.FLOAT_ARRAY,
            getTypeName(Types.FLOAT) + "[]");
        this.registerColumnType(CustomTypes.DOUBLE_ARRAY,
            getTypeName(Types.DOUBLE) + "[]");
        this.registerColumnType(CustomTypes.SMALLINT_ARRAY,
            getTypeName(Types.SMALLINT) + "[]");
        this.registerColumnType(CustomTypes.INTEGER_ARRAY,
            getTypeName(Types.INTEGER) + "[]");
        this.registerColumnType(CustomTypes.BIGINT_ARRAY,
            getTypeName(Types.BIGINT) + "[]");
        this.registerColumnType(CustomTypes.LONGVARCHAR_ARRAY,
            getTypeName(Types.LONGVARCHAR) + "[]");
    }
}
