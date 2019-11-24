package odata_jdbc.jdbc;

public enum SqlStatementType {

    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    String statementStartWith;

    SqlStatementType(String statementStartWith) {
        this.statementStartWith = statementStartWith;
    }

    static SqlStatementType from(String statementStartWith) {
        for (SqlStatementType value : values()) {
            if (value.statementStartWith.equalsIgnoreCase(statementStartWith)) {
                return value;
            }
        }
        throw new IllegalArgumentException(statementStartWith);
    }
}
