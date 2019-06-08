package odata_jdbc.jdbc;

import odata_jdbc.uitl.StringUtil;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.stream.Collectors;

public class SelectSqlParser {

    public SqlParseResult parse(String sql) throws SQLSyntaxErrorException {
        String oneLineLowerCaseSql = normalize(sql);

        validate(oneLineLowerCaseSql);

        Map<String, String> sqlClauseMap = splitClause(oneLineLowerCaseSql);

        String fromTable = extractFromTable(sqlClauseMap.get("from"));
        List<SelectColumn> selectColumns = parseSelectColumnsClause(sqlClauseMap.get("select"));

        SqlParseResult result = new SqlParseResult(fromTable, selectColumns);

        String whereClause = sqlClauseMap.get("where");
        if (!whereClause.isEmpty()) {
            result.setWhereClause(whereClause);
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
        if (!startsWithIgnoreCase(sql, "select ")) {
            // "SELECT " で始まっていなければエラー(WITH句は非サポート)
            throw new SQLSyntaxErrorException("not supported sql. required starts with 'SELECT '");
        }
        if (StringUtil.indexOfIgnoreCase(sql, " select ", 1) != -1) {
            // 先頭以外に " select " があればエラー(サブクエリやインラインビュー、UNION等は非サポート)
            throw new SQLSyntaxErrorException("not support multiple 'SELECT' clause");
        }

        int fromIndex = StringUtil.indexOfIgnoreCase(sql, " from ");
        if (fromIndex == -1) {
            // " FROM " がなければエラー
            throw new SQLSyntaxErrorException("not supported sql. required 'FROM' clause");
        }
        if (StringUtil.indexOfIgnoreCase(sql, " from ", fromIndex + 1) != -1) {
            // " FROM " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
            throw new SQLSyntaxErrorException("not support multiple 'FROM' clause");
        }

        int whereIndex = StringUtil.indexOfIgnoreCase(sql, " where ");
        if (whereIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " where ", whereIndex + 1) != -1) {
                // " WHERE " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'WHERE' clause");
            }
            if (whereIndex < fromIndex) {
                // " WHERE " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int groupByIndex = StringUtil.indexOfIgnoreCase(sql," group by ");
        if (groupByIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " group by ", groupByIndex + 1) != -1) {
                // " GROUP BY " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'GROUP BY' clause");
            }
            if (groupByIndex < fromIndex) {
                // " GROUP BY " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (whereIndex != -1 && groupByIndex < whereIndex) {
                // " GROUP BY " が " WHERE " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int havingIndex = StringUtil.indexOfIgnoreCase(sql," having ");
        if (havingIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " having ", havingIndex + 1) != -1) {
                // " HAVING " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'HAVING' clause");
            }
            if (havingIndex < fromIndex) {
                // " HAVING " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (whereIndex != -1 && havingIndex < whereIndex) {
                // " HAVING " が " WHERE " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (groupByIndex != -1 && havingIndex < groupByIndex) {
                // " HAVING " が " GROUP BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int orderByIndex = StringUtil.indexOfIgnoreCase(sql," order by ");
        if (orderByIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " order by ", orderByIndex + 1) != -1) {
                // " ORDER BY " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'ORDER BY' clause");
            }
            if (orderByIndex < fromIndex) {
                // " ORDER BY " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (whereIndex != -1 && orderByIndex < whereIndex) {
                // " ORDER BY " が " WHERE " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (groupByIndex != -1 && orderByIndex < groupByIndex) {
                // " ORDER BY " が " GROUP BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (havingIndex != -1 && orderByIndex < havingIndex) {
                // " ORDER BY " が " HAVING " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int offsetIndex = StringUtil.indexOfIgnoreCase(sql," offset ");
        if (offsetIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " offset ", offsetIndex + 1) != -1) {
                // " OFFSET " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'OFFSET' clause");
            }
            if (offsetIndex < fromIndex) {
                // " OFFSET " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (whereIndex != -1 && offsetIndex < whereIndex) {
                // " OFFSET " が " WHERE " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (groupByIndex != -1 && offsetIndex < groupByIndex) {
                // " OFFSET " が " GROUP BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (havingIndex != -1 && offsetIndex < havingIndex) {
                // " OFFSET " が " HAVING " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (orderByIndex != -1 && offsetIndex < orderByIndex) {
                // " OFFSET " が " ORDER BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int fetchIndex = StringUtil.indexOfIgnoreCase(sql," fetch ");
        if (fetchIndex != -1) {
            if (StringUtil.indexOfIgnoreCase(sql, " fetch ", fetchIndex + 1) != -1) {
                // " FETCH " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'FETCH' clause");
            }
            if (fetchIndex < fromIndex) {
                // " FETCH " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (whereIndex != -1 && fetchIndex < whereIndex) {
                // " FETCH " が " WHERE " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (groupByIndex != -1 && fetchIndex < groupByIndex) {
                // " FETCH " が " GROUP BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (havingIndex != -1 && fetchIndex < havingIndex) {
                // " FETCH " が " HAVING " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (orderByIndex != -1 && fetchIndex < orderByIndex) {
                // " FETCH " が " ORDER BY " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
            if (offsetIndex != -1 && fetchIndex < offsetIndex) {
                // " FETCH " が " OFFSET " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        if (StringUtil.indexOfIgnoreCase(sql," limit ") != -1) {
            throw new SQLSyntaxErrorException("not support 'LIMIT' clause. use 'FETCH' clause");
        }
    }

    private Map<String, String> splitClause(String sql) throws SQLSyntaxErrorException {
        Map<String, String> result = new HashMap<>();

        int fromIndex = StringUtil.indexOfIgnoreCase(sql, " from ");
        String selectClause = sql.substring("select ".length(), fromIndex).trim();
        if (StringUtil.indexOfIgnoreCase(selectClause, "distinct ") != -1) {
            throw new SQLSyntaxErrorException("not supported distinct");
        }
        result.put("select", selectClause);

        String fromClause = extractFromClause(sql);
        if (StringUtil.indexOfIgnoreCase(fromClause, " join ") != -1 || fromClause.indexOf(",") != -1) {
            throw new SQLSyntaxErrorException("not supported join sql");
        }
        result.put("from", fromClause);
        result.put("where", extractWhereClause(sql));
        result.put("group by", extractGroupByClause(sql));
        result.put("having", extractHavingClause(sql));
        result.put("order by", extractOrderByClause(sql));
        result.put("offset", extractOffsetClause(sql));
        result.put("fetch", extractFetchClause(sql));
        return result;
    }

    private String substringToClause(String sql, String... clauses) {
        for (String clause : clauses) {
            int index = StringUtil.indexOfIgnoreCase(sql, clause);
            if (index != -1) {
                return sql.substring(0, index).trim();
            }
        }
        return sql;
    }

    private String extractFromClause(String sql) {
        int fromIndex = StringUtil.indexOfIgnoreCase(sql, " from ");
        String fromClause = sql.substring(fromIndex + " from ".length()).trim();
        return substringToClause(fromClause, " where ", " group by ", " having ", " order by ", " offset ", " fetch ");
    }

    private String extractWhereClause(String sql) {
        int whereIndex = StringUtil.indexOfIgnoreCase(sql, " where ");
        if (whereIndex == -1) {
            return "";
        }
        String whereClause = sql.substring(whereIndex + " where ".length()).trim();
        return substringToClause(whereClause, " group by ", " having ", " order by ", " offset ", " fetch ");
    }

    private String extractGroupByClause(String sql) {
        int groupByIndex = StringUtil.indexOfIgnoreCase(sql," group by ");
        if (groupByIndex != -1) {
            return "";
        }
        String groupByClause = sql.substring(groupByIndex + " group by ".length()).trim();
        return substringToClause(groupByClause, " having ", " order by ", " offset ", " fetch ");
    }

    private String extractHavingClause(String sql) {
        int havingIndex = StringUtil.indexOfIgnoreCase(sql," having ");
        if (havingIndex != -1) {
            return "";
        }
        String havingClause = sql.substring(havingIndex + " having ".length()).trim();
        return substringToClause(havingClause, " order by ", " offset ", " fetch ");
    }

    private String extractOrderByClause(String sql) {
        int orderByIndex = StringUtil.indexOfIgnoreCase(sql," order by ");
        if (orderByIndex != -1) {
            return "";
        }
        String orderByClause = sql.substring(orderByIndex + " order by ".length()).trim();
        return substringToClause(orderByClause, " offset ", " fetch ");
    }

    private String extractOffsetClause(String sql) {
        int offsetIndex = StringUtil.indexOfIgnoreCase(sql," offset ");
        if (offsetIndex != -1) {
            return "";
        }
        String offsetClause = sql.substring(offsetIndex + " offset ".length()).trim();
        return substringToClause(offsetClause, " fetch ");
    }

    private String extractFetchClause(String sql) {
        int fetchIndex = StringUtil.indexOfIgnoreCase(sql, " fetch ");
        if (fetchIndex != -1) {
            return "";
        }
        return sql.substring(fetchIndex + " fetch ".length()).trim();
    }

    private String extractFromTable(String fromClause) throws SQLSyntaxErrorException {
        int fromTableSpaceIndex = fromClause.indexOf(" ");
        if (fromTableSpaceIndex != -1) {
            return fromClause.substring(0, fromTableSpaceIndex);
        }
        return fromClause;
    }

    private List<SelectColumn> parseSelectColumnsClause(String selectColumnsClause) throws SQLSyntaxErrorException {
        List<SelectColumn> results = new ArrayList<>();

        if (selectColumnsClause.trim().startsWith("*")) {
            if (!selectColumnsClause.trim().equals("*")) {
                throw new SQLSyntaxErrorException();
            }
            results.add(new SelectColumn("*", "*"));
            return results;
        }
        return Arrays.asList(selectColumnsClause.split(",")).stream().map(columnClause -> {
            int asIndex = StringUtil.indexOfIgnoreCase(columnClause, " as ");
            if (asIndex == -1) {
                return new SelectColumn(columnClause.trim(), columnClause.trim());
            }
            return new SelectColumn(columnClause.substring(0, asIndex).trim(), columnClause.substring(asIndex + " as ".length()).trim());
        }).collect(Collectors.toList());
    }

    private boolean startsWithIgnoreCase(String target, String value) {
        return target.substring(0, value.length()).equalsIgnoreCase(value);
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
