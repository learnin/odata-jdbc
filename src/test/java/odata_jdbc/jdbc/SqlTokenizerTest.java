package odata_jdbc.jdbc;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SqlTokenizerTest {

    private List<SqlToken> createSqlTokenList(String... values) {
        return Arrays.asList(values).stream().map(value -> new SqlToken(value)).collect(Collectors.toList());
    }

    @Test
    void tokenize() {
        String sql = "SELECT id,name, value  FROM hoge\n" + "WHERE  foo = 1 AND bar=2 AND baz>=3 ORDER BY id ASC";
        SqlTokenizer sut = new SqlTokenizer(sql);
        List<SqlToken> actual = sut.tokenize();
        List<SqlToken> expected = createSqlTokenList("SELECT", "id", ",", "name", ",", "value", "FROM", "hoge",
                "WHERE", "foo", "=", "1", "AND", "bar", "=", "2", "AND", "baz", ">=", "3", "ORDER BY", "id", "ASC");
        assertEquals(expected, actual);
    }
}