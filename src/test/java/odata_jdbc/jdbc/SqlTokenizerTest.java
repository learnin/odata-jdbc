package odata_jdbc.jdbc;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SqlTokenizerTest {

    private SqlTokens createSqlTokenList(String... values) {
        return new SqlTokens(Arrays.asList(values).stream().map(value -> new SqlToken(value)).collect(Collectors.toList()));
    }

    @Test
    void tokenize() {
        String sql = "SELECT id,name, value  FROM hoge\n" + "WHERE  foo = 1 AND bar=2 AND baz>=3 ORDER BY id ASC";
        SqlTokenizer sut = new SqlTokenizer(sql);
        SqlTokens actual = sut.tokenize();
        SqlTokens expected = createSqlTokenList("SELECT", "id", ",", "name", ",", "value", "FROM", "hoge",
                "WHERE", "foo", "=", "1", "AND", "bar", "=", "2", "AND", "baz", ">=", "3", "ORDER BY", "id", "ASC");
        assertEquals(expected, actual);
    }
}