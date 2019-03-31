package odata_jdbc.jdbc;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectSqlParser {

    public SqlParseResult parse(String sql) throws SQLSyntaxErrorException {
        String oneLineLowerCaseSql = normalize(sql);
        if (!startsWithIgnoreCase(oneLineLowerCaseSql, "select ")) {
            throw new SQLSyntaxErrorException("not supported sql");
        }
        String fromTable = extractFromTable(oneLineLowerCaseSql);
        List<SelectColumn> selectColumns = parseSelectColumnsPhrase(extractSelectColumnsPhrase(oneLineLowerCaseSql));

        return new SqlParseResult(fromTable, selectColumns);
    }

    private String normalize(String sql) {
        return sql
                .replaceAll("\\r\\n|\\r|\\n", "\\n") // 改行コードをLFに統一
                .replaceAll("//.*\\n", "\\n") // 1行コメントを除去
                .replaceAll("\\n|\\t", " ") // 改行コード、タブをスペースに置換
                .replaceAll("/\\*.*\\*/", ""); // 複数行コメントを除去
    }

    private String extractFromTable(String sql) throws SQLSyntaxErrorException {
        int fromIndex = indexOfIgnoreCase(sql, " from ");
        if (fromIndex == -1) {
            throw new SQLSyntaxErrorException();
        }
        if (indexOfIgnoreCase(sql, " from ", fromIndex + 1) != -1) {
            throw new SQLSyntaxErrorException("not supported subquery or inline view sql");
        }
        String fromTablePhrase = sql.substring(fromIndex + " from ".length()).trim();
        int whereIndex = indexOfIgnoreCase(fromTablePhrase, " where ");
        if (whereIndex != -1) {
            fromTablePhrase = fromTablePhrase.substring(0, whereIndex).trim();
        }
        int groupByIndex = indexOfIgnoreCase(fromTablePhrase," group by ");
        if (groupByIndex != -1) {
            fromTablePhrase = fromTablePhrase.substring(0, groupByIndex).trim();
        }
        int havingIndex = indexOfIgnoreCase(fromTablePhrase," having ");
        if (havingIndex != -1) {
            fromTablePhrase = fromTablePhrase.substring(0, havingIndex).trim();
        }
        int orderByIndex = indexOfIgnoreCase(fromTablePhrase," order by ");
        if (orderByIndex != -1) {
            fromTablePhrase = fromTablePhrase.substring(0, orderByIndex).trim();
        }
        if (indexOfIgnoreCase(fromTablePhrase, " join ") != -1 || fromTablePhrase.indexOf(",") != -1) {
            throw new SQLSyntaxErrorException("not supported join sql");
        }
        int fromTableSpaceIndex = fromTablePhrase.indexOf(" ");
        if (fromTableSpaceIndex != -1) {
            return fromTablePhrase.substring(0, fromTableSpaceIndex);
        }
        return fromTablePhrase;
    }

    private String extractSelectColumnsPhrase(String sql) {
        int fromIndex = indexOfIgnoreCase(sql, " from ");
        return sql.substring("select ".length(), fromIndex).trim();
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
    }
}
