package odata_jdbc.jdbc;

import java.util.Objects;

public class SqlToken {

    private String value;

    SqlToken(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlToken sqlToken = (SqlToken) o;
        return Objects.equals(value, sqlToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
