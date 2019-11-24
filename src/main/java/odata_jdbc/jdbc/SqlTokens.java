package odata_jdbc.jdbc;

import java.util.List;
import java.util.Objects;

public class SqlTokens {

    private final List<SqlToken> tokens;

    SqlTokens(List<SqlToken> tokens) {
        this.tokens = tokens;
    }

    SqlStatementType statementType() {
        return SqlStatementType.from(tokens.get(0).value());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlTokens sqlTokens = (SqlTokens) o;
        return tokens.equals(sqlTokens.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokens);
    }
}
