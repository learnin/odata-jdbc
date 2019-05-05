package odata_jdbc.jdbc;

import java.util.List;

public class SqlParseResult {

    private final String fromTable;
    private final List<SelectSqlParser.SelectColumn> selectColumns;
    private String whereClause;

    // TODO: ODataUrlBuilderで値の中の = まで eq に置換しないようにするために、WHERE句の内容はwhereClauseでもつのをやめて、これでもつように修正する
    private List<WherePredicate> wherePredicates;

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
