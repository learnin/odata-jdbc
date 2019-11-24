package odata_jdbc.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SqlTokenizer {

    // 前後にwhitespaceを必須とするキーワード
    private static final List<String> KEYWORDS = Arrays.asList("SELECT", "ORDER BY");
    // 前後にwhitespaceを必須とはしないキーワード（文字数が多いものからマッチングさせる必要があるため、文字数の多い順でソート）
    private static final List<String> OPERATORS = Arrays.asList(",", "(", ")", "=", ">", ">=", "<", "<=", "<>", "!=").stream().sorted((o1, o2) -> o2.length() - o1.length()).collect(Collectors.toList());

    private String sql;
    private int pos;

    SqlTokenizer(String sql) {
        this.sql = sql;
        this.pos = 0;
    }

    SqlTokens tokenize() {
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
                List<String> beforeNextKeywordStrings = readToNextKeyword();
                beforeNextKeywordStrings.stream().forEach(beforeNextKeywordString -> results.add(new SqlToken(beforeNextKeywordString)));
            }
        }
        return new SqlTokens(results);
    }

    List<String> readToNextKeyword() {
        List<String> results = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        while (pos < sql.length() && !Character.isWhitespace(sql.codePointAt(pos))) {
            Optional<String> matchOperator = OPERATORS.stream().filter(operator -> {
                int operatorLength = operator.length();
                return sql.length() >= pos + operatorLength && operator.equals(sql.substring(pos, pos + operatorLength));
            }).findFirst();
            if (matchOperator.isPresent()) {
                if (result.length() > 0) {
                    results.add(result.toString());
                }
                String operator = matchOperator.get();
                results.add(operator);
                pos += operator.length();
                return results;
            }
            int charCount = Character.charCount(sql.codePointAt(pos));
            result.append(sql.substring(pos, pos + charCount));
            pos += charCount;
        }
        results.add(result.toString());
        return results;
    }

    void skipSpace() {
        while (Character.isWhitespace(sql.codePointAt(pos))) {
            pos++;
        }
    }

}
