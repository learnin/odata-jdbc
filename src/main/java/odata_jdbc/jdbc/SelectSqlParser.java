package odata_jdbc.jdbc;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.stream.Collectors;

public class SelectSqlParser {

    public SqlParseResult parse(String sql) throws SQLSyntaxErrorException {
        String oneLineLowerCaseSql = normalize(sql);

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
        if (!startsWithIgnoreCase(sql, "select ")) {
            // "SELECT " で始まっていなければエラー(WITH句は非サポート)
            throw new SQLSyntaxErrorException("not supported sql. required starts with 'SELECT '");
        }
        if (indexOfIgnoreCase(sql, " select ", 1) != -1) {
            // 先頭以外に " select " があればエラー(サブクエリやインラインビュー、UNION等は非サポート)
            throw new SQLSyntaxErrorException("not support multiple 'SELECT' phrase");
        }

        int fromIndex = indexOfIgnoreCase(sql, " from ");
        if (fromIndex == -1) {
            // " FROM " がなければエラー
            throw new SQLSyntaxErrorException("not supported sql. required 'FROM' phrase");
        }
        if (indexOfIgnoreCase(sql, " from ", fromIndex + 1) != -1) {
            // " FROM " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
            throw new SQLSyntaxErrorException("not support multiple 'FROM' phrase");
        }

        int whereIndex = indexOfIgnoreCase(sql, " where ");
        if (whereIndex != -1) {
            if (indexOfIgnoreCase(sql, " where ", whereIndex + 1) != -1) {
                // " WHERE " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'WHERE' phrase");
            }
            if (whereIndex < fromIndex) {
                // " WHERE " が " FROM " よりも前にあればエラー
                throw new SQLSyntaxErrorException("illegal syntax sql");
            }
        }

        int groupByIndex = indexOfIgnoreCase(sql," group by ");
        if (groupByIndex != -1) {
            if (indexOfIgnoreCase(sql, " group by ", groupByIndex + 1) != -1) {
                // " GROUP BY " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'GROUP BY' phrase");
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

        int havingIndex = indexOfIgnoreCase(sql," having ");
        if (havingIndex != -1) {
            if (indexOfIgnoreCase(sql, " having ", havingIndex + 1) != -1) {
                // " HAVING " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'HAVING' phrase");
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

        int orderByIndex = indexOfIgnoreCase(sql," order by ");
        if (orderByIndex != -1) {
            if (indexOfIgnoreCase(sql, " order by ", orderByIndex + 1) != -1) {
                // " ORDER BY " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'ORDER BY' phrase");
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

        int offsetIndex = indexOfIgnoreCase(sql," offset ");
        if (offsetIndex != -1) {
            if (indexOfIgnoreCase(sql, " offset ", offsetIndex + 1) != -1) {
                // " OFFSET " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'OFFSET' phrase");
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

        int fetchIndex = indexOfIgnoreCase(sql," fetch ");
        if (fetchIndex != -1) {
            if (indexOfIgnoreCase(sql, " fetch ", fetchIndex + 1) != -1) {
                // " FETCH " が複数あればエラー(サブクエリやインラインビュー、UNION等は非サポート)
                throw new SQLSyntaxErrorException("not support multiple 'FETCH' phrase");
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

        if (indexOfIgnoreCase(sql," limit ") != -1) {
            throw new SQLSyntaxErrorException("not support 'LIMIT' phrase. use 'FETCH' phrase");
        }
    }

    private Map<String, String> splitPhrase(String sql) throws SQLSyntaxErrorException {
        Map<String, String> result = new HashMap<>();

        int fromIndex = indexOfIgnoreCase(sql, " from ");
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
        result.put("offset", extractOffsetPhrase(sql));
        result.put("fetch", extractFetchPhrase(sql));
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
        int offsetIndex = indexOfIgnoreCase(fromPhrase," offset ");
        if (offsetIndex != -1) {
            return fromPhrase.substring(0, offsetIndex).trim();
        }
        int fetchIndex = indexOfIgnoreCase(fromPhrase," fetch ");
        if (fetchIndex != -1) {
            return fromPhrase.substring(0, fetchIndex).trim();
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
        int offsetIndex = indexOfIgnoreCase(wherePhrase," offset ");
        if (offsetIndex != -1) {
            return wherePhrase.substring(0, offsetIndex).trim();
        }
        int fetchIndex = indexOfIgnoreCase(wherePhrase," fetch ");
        if (fetchIndex != -1) {
            return wherePhrase.substring(0, fetchIndex).trim();
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
        int offsetIndex = indexOfIgnoreCase(groupByPhrase," offset ");
        if (offsetIndex != -1) {
            return groupByPhrase.substring(0, offsetIndex).trim();
        }
        int fetchIndex = indexOfIgnoreCase(groupByPhrase," fetch ");
        if (fetchIndex != -1) {
            return groupByPhrase.substring(0, fetchIndex).trim();
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
        int offsetIndex = indexOfIgnoreCase(havingPhrase," offset ");
        if (offsetIndex != -1) {
            return havingPhrase.substring(0, offsetIndex).trim();
        }
        int fetchIndex = indexOfIgnoreCase(havingPhrase," fetch ");
        if (fetchIndex != -1) {
            return havingPhrase.substring(0, fetchIndex).trim();
        }
        return havingPhrase;
    }

    private String extractOrderByPhrase(String sql) {
        int orderByIndex = indexOfIgnoreCase(sql," order by ");
        if (orderByIndex != -1) {
            return "";
        }
        String orderByPhrase = sql.substring(orderByIndex + " order by ".length()).trim();
        int offsetIndex = indexOfIgnoreCase(orderByPhrase," offset ");
        if (offsetIndex != -1) {
            return orderByPhrase.substring(0, offsetIndex).trim();
        }
        int fetchIndex = indexOfIgnoreCase(orderByPhrase," fetch ");
        if (fetchIndex != -1) {
            return orderByPhrase.substring(0, fetchIndex).trim();
        }
        return orderByPhrase;
    }

    private String extractOffsetPhrase(String sql) {
        int offsetIndex = indexOfIgnoreCase(sql," offset ");
        if (offsetIndex != -1) {
            return "";
        }
        String offsetPhrase = sql.substring(offsetIndex + " offset ".length()).trim();
        int fetchIndex = indexOfIgnoreCase(offsetPhrase," fetch ");
        if (fetchIndex != -1) {
            return offsetPhrase.substring(0, fetchIndex).trim();
        }
        return offsetPhrase;
    }

    private String extractFetchPhrase(String sql) {
        int fetchIndex = indexOfIgnoreCase(sql, " fetch ");
        if (fetchIndex != -1) {
            return "";
        }
        return sql.substring(fetchIndex + " fetch ".length()).trim();
    }

    private String extractFromTable(String fromPhrase) throws SQLSyntaxErrorException {
        int fromTableSpaceIndex = fromPhrase.indexOf(" ");
        if (fromTableSpaceIndex != -1) {
            return fromPhrase.substring(0, fromTableSpaceIndex);
        }
        return fromPhrase;
        // FIXME: FROM dbname.schema.table のようなSQLの場合の考慮
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
