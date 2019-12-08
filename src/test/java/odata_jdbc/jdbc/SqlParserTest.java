package odata_jdbc.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlParserTest {

    @Test
    void parse() throws SqlParseException {
//        String sql = "SELECT DISTINCT id,name, value  FROM hoge\n" + "WHERE  foo = 1 AND bar=2 AND baz>=3 ORDER BY id ASC";
        String sql = "SELECT * FROM hoge\n" + "WHERE  foo = 1 AND bar=2 AND baz>=3 ORDER BY id ASC";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);
        SqlParser sut = new SqlParser(tokenizer.tokenize());
        SelectStatement actual = (SelectStatement) sut.parse();
        SelectStatement expected = new SelectStatement();
//        expected.setQuantifier("DISTINCT");
        expected.addSelectList("*");
        assertEquals(expected, actual);
    }
}