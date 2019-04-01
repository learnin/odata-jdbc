package odata_jdbc.jdbc;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.stream.Collectors;

public class SelectSqlParser {

    public SqlParseResult parse(String sql) throws SQLSyntaxErrorException {
        String oneLineLowerCaseSql = normalize(sql);
        if (!startsWithIgnoreCase(oneLineLowerCaseSql, "select ")) {
            throw new SQLSyntaxErrorException("not supported sql");
        }

        validate(oneLineLowerCaseSql);

        Map<String, String> sqlPhraseMap = splitPhrase(oneLineLowerCaseSql);

        String fromTable = extractFromTable(sqlPhraseMap.get("from"));
        List<SelectColumn> selectColumns = parseSelectColumnsPhrase(sqlPhraseMap.get("select"));

        SqlParseResult result = new SqlParseResult(fromTable, selectColumns);

        String wherePhrase = sqlPhraseMap.get("where");
        if (!wherePhrase.isEmpty()) {
            result.setWherePhrase(wherePhrase);
        }
        return result;
    }

    private String normalize(String sql) {
        return sql
                .replaceAll("\\r\\n|\\r|\\n", "\\n") // 改行コードをLFに統一
                .replaceAll("//.*\\n", "\\n") // 1行コメントを除去
                .replaceAll("\\n|\\t", " ") // 改行コード、タブをスペースに置換
                .replaceAll("/\\*.*\\*/", ""); // 複数行コメントを除去
    }

    private void validate(String sql) throws SQLSyntaxErrorException {
        // TODO: 各句の順序、1度だけかをチェック
    }

    private Map<String, String> splitPhrase(String sql) throws SQLSyntaxErrorException {
        Map<String, String> result = new HashMap<>();

        int fromIndex = indexOfIgnoreCase(sql, " from ");
        if (fromIndex == -1) {
            throw new SQLSyntaxErrorException();
        }
        if (indexOfIgnoreCase(sql, " from ", fromIndex + 1) != -1) {
            throw new SQLSyntaxErrorException("not supported subquery or inline view sql");
        }

        result.put("select", sql.substring("select ".length(), fromIndex).trim());

        String fromPhrase = extractFromPhrase(sql);
        if (indexOfIgnoreCase(fromPhrase, " join ") != -1 || fromPhrase.indexOf(",") != -1) {
            throw new SQLSyntaxErrorException("not supported join sql");
        }
        result.put("from", fromPhrase);
        result.put("where", extractWherePhrase(sql));
        result.put("group by", extractGroupByPhrase(sql));
        result.put("having", extractHavingPhrase(sql));
        result.put("order by", extractOrderByPhrase(sql));
        return result;
    }

    private String extractFromPhrase(String sql) {
        int fromIndex = indexOfIgnoreCase(sql, " from ");
        String fromPhrase = sql.substring(fromIndex + " from ".length()).trim();
        int whereIndex = indexOfIgnoreCase(fromPhrase, " where ");
        if (whereIndex != -1) {
            return fromPhrase.substring(0, whereIndex).trim();
        }
        int groupByIndex = indexOfIgnoreCase(fromPhrase," group by ");
        if (groupByIndex != -1) {
            return fromPhrase.substring(0, groupByIndex).trim();
        }
        int havingIndex = indexOfIgnoreCase(fromPhrase," having ");
        if (havingIndex != -1) {
            return fromPhrase.substring(0, havingIndex).trim();
        }
        int orderByIndex = indexOfIgnoreCase(fromPhrase," order by ");
        if (orderByIndex != -1) {
            return fromPhrase.substring(0, orderByIndex).trim();
        }
        return fromPhrase;
    }

    private String extractWherePhrase(String sql) {
        int whereIndex = indexOfIgnoreCase(sql, " where ");
        if (whereIndex == -1) {
            return "";
        }
        String wherePhrase = sql.substring(whereIndex + " where ".length()).trim();
        int groupByIndex = indexOfIgnoreCase(wherePhrase," group by ");
        if (groupByIndex != -1) {
            return wherePhrase.substring(0, groupByIndex).trim();
        }
        int havingIndex = indexOfIgnoreCase(wherePhrase," having ");
        if (havingIndex != -1) {
            return wherePhrase.substring(0, havingIndex).trim();
        }
        int orderByIndex = indexOfIgnoreCase(wherePhrase," order by ");
        if (orderByIndex != -1) {
            return wherePhrase.substring(0, orderByIndex).trim();
        }
        return wherePhrase;
    }

    private String extractGroupByPhrase(String sql) {
        int groupByIndex = indexOfIgnoreCase(sql," group by ");
        if (groupByIndex != -1) {
            return "";
        }
        String groupByPhrase = sql.substring(groupByIndex + " group by ".length()).trim();
        int havingIndex = indexOfIgnoreCase(groupByPhrase," having ");
        if (havingIndex != -1) {
            return groupByPhrase.substring(0, havingIndex).trim();
        }
        int orderByIndex = indexOfIgnoreCase(groupByPhrase," order by ");
        if (orderByIndex != -1) {
            return groupByPhrase.substring(0, orderByIndex).trim();
        }
        return groupByPhrase;
    }

    private String extractHavingPhrase(String sql) {
        int havingIndex = indexOfIgnoreCase(sql," having ");
        if (havingIndex != -1) {
            return "";
        }
        String havingPhrase = sql.substring(havingIndex + " having ".length()).trim();
        int orderByIndex = indexOfIgnoreCase(havingPhrase," order by ");
        if (orderByIndex != -1) {
            return havingPhrase.substring(0, orderByIndex).trim();
        }
        return havingPhrase;
    }

    private String extractOrderByPhrase(String sql) {
        int orderByIndex = indexOfIgnoreCase(sql," order by ");
        if (orderByIndex != -1) {
            return "";
        }
        return sql.substring(orderByIndex + " order by ".length()).trim();
    }

    private String extractFromTable(String fromPhrase) throws SQLSyntaxErrorException {
        int fromTableSpaceIndex = fromPhrase.indexOf(" ");
        if (fromTableSpaceIndex != -1) {
            return fromPhrase.substring(0, fromTableSpaceIndex);
        }
        return fromPhrase;
    }

    private List<SelectColumn> parseSelectColumnsPhrase(String selectColumnsPhrase) throws SQLSyntaxErrorException {
        List<SelectColumn> results = new ArrayList<>();

        if (selectColumnsPhrase.trim().startsWith("*")) {
            if (!selectColumnsPhrase.trim().equals("*")) {
                throw new SQLSyntaxErrorException();
            }
            results.add(new SelectColumn("*", "*"));
            return results;
        }
        return Arrays.asList(selectColumnsPhrase.split(",")).stream().map(columnPhrase -> {
            int asIndex = indexOfIgnoreCase(columnPhrase, " as ");
            if (asIndex == -1) {
                return new SelectColumn(columnPhrase.trim(), columnPhrase.trim());
            }
            return new SelectColumn(columnPhrase.substring(0, asIndex).trim(), columnPhrase.substring(asIndex + " as ".length()).trim());
        }).collect(Collectors.toList());
    }

    private boolean startsWithIgnoreCase(String target, String value) {
        return target.substring(0, value.length()).equalsIgnoreCase(value);
    }

    private int indexOfIgnoreCase(String target, String value) {
        return target.toLowerCase().indexOf(value.toLowerCase());
    }

    private int indexOfIgnoreCase(String target, String value, int fromIndex) {
        return target.toLowerCase().indexOf(value.toLowerCase(), fromIndex);
    }

    public static class SelectColumn {

        private final String column;
        private final String columnAs;

        public SelectColumn(String column, String columnAs) {
            this.column = column;
            this.columnAs = columnAs;
        }

        public String column() {
            return column;
        }
    }
}
