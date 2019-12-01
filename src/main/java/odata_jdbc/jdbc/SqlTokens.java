package odata_jdbc.jdbc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SqlTokens {

    private final List<SqlToken> tokens;
    private int pos;

    SqlTokens(List<SqlToken> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    SqlStatementType statementType() {
        return SqlStatementType.from(tokens.get(0).value());
    }

    Optional<SqlToken> peek() {
        return hasNext() ? Optional.of(tokens.get(pos)) : Optional.empty();
    }

    Optional<SqlToken> next() {
        Optional<SqlToken> result = peek();
        if (result.isPresent()) {
            pos++;
        }
        return result;
    }

    void nextPos() {
        if (hasNext()) {
            pos++;
        }
    }

    private boolean hasNext() {
        return pos < tokens.size();
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
