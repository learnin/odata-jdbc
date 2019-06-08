package odata_jdbc.jdbc;

import java.util.List;

public class SqlParseResult {

    private final String fromTable;
    private final List<SelectSqlParser.SelectColumn> selectColumns;
    private String whereClause;

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

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String whereClause() {
        return whereClause;
    }
}
