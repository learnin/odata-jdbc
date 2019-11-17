package odata_jdbc.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SqlTokenizer {

    private static final List<String> KEYWORDS = Arrays.asList("SELECT");
    private static final List<String> OPERATORS = Arrays.asList(",", "(", ")");

    private String sql;
    private int pos;

    SqlTokenizer(String sql) {
        this.sql = sql;
        this.pos = 0;
    }

    List<SqlToken> tokenize() {
        List<SqlToken> results = new ArrayList<>();

        for (int sqlLength = sql.codePointCount(0, sql.length()); pos < sqlLength;) {
            skipSpace();

            Optional<String> matchKeyword = KEYWORDS.stream().filter(keyword -> {
                int keywordLength = keyword.length();
                return sql.length() >= pos + keywordLength && keyword.equals(sql.substring(pos, pos + keywordLength).toUpperCase());
            }).findFirst();
            if (matchKeyword.isPresent()) {
                String keyword = matchKeyword.get();
                results.add(new SqlToken(keyword));
                pos += keyword.length();
            } else {
                String s = readToNextKeyword();
                if (!s.isEmpty()) {
                    results.add(new SqlToken(s));
                }
            }
        }
        return results;
    }

    String readToNextKeyword() {
        StringBuilder result = new StringBuilder();
        while (pos < sql.length() && !Character.isWhitespace(sql.codePointAt(pos)) && !OPERATORS.contains(sql.substring(pos, pos + 1))) {
            int charCount = Character.charCount(sql.codePointAt(pos));
            result.append(sql.substring(pos, pos + charCount));
            pos += charCount;
        }
        return result.toString();
    }

    void skipSpace() {
        while (Character.isWhitespace(sql.codePointAt(pos))) {
            pos++;
        }
    }

}
