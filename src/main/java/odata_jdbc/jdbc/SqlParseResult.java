package odata_jdbc.jdbc;

import java.util.List;

public class SqlParseResult {

    private final String fromTable;
    private final List<SelectSqlParser.SelectColumn> selectColumns;

    public SqlParseResult(String fromTable, List<SelectSqlParser.SelectColumn> selectColumns) {
        this.fromTable = fromTable;
        this.selectColumns = selectColumns;
    }

    public String from() {
        return fromTable;
    }

    public List<SelectSqlParser.SelectColumn> selectColumns() {
        return selectColumns;
    }
}
